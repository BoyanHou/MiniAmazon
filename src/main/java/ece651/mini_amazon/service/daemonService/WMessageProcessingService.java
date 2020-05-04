package ece651.mini_amazon.service.daemonService;

import ece651.mini_amazon.dao.PackageDao;
import ece651.mini_amazon.dao.ProductDao;
import ece651.mini_amazon.exceptions.serviceException.NoSuchPackageException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchWarehouseException;
import ece651.mini_amazon.service.webService.BackingService;
import ece651.mini_amazon.service.webService.ShoppingService;
import ece651.mini_amazon.model.Package;
import ece651.mini_amazon.utils.commChannel.CommChannel;
import ece651.mini_amazon.utils.concurrentTools.Notifier;
import ece651.mini_amazon.utils.tools.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import protobuf_generated.WorldAmazon;

import java.util.ArrayList;
import java.util.List;

@Service
public class WMessageProcessingService extends MessageProcessingService {
    @Autowired
    BackingService backingService;
    @Autowired
    DaemonService daemonService;
    @Autowired
    ShoppingService shoppingService;
    @Autowired
    PackageDao pkgDao;

    public void process(WorldAmazon.AResponses msg,
                        CommChannel commChannel,
                        DaemonService.AckWaitList ackWaitList) {
        // repeated APurchaseMore arrived = 1;
        this.processArrived(msg, commChannel);

        //  repeated APacked ready = 2;
        this.processAPacked(msg, commChannel);

        //  repeated ALoaded loaded = 3;
        this.processedALoaded(msg, commChannel);

//        optional bool finished = 4;
//        repeated AErr error = 5;

        // repeated int64 acks = 6;
        this.processAcks(msg, ackWaitList);

        // repeated APackage packagestatus = 7;
        this.processAPackage(msg, commChannel);
    }


    // repeated APurchaseMore arrived = 1;
    protected void processArrived(WorldAmazon.AResponses msg,
                                 CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getArrivedCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to World: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getArrived(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        WorldAmazon.ACommands aCommands = WorldAmazon.ACommands.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aCommands);
        this.threadPool.submit(ackSendingTask);

        // step3: check each "APurchaseMore arrived" message, skip if is old seqnum; update related WareHose stock info
        for (int i = 0; i < count; i++) {
            WorldAmazon.APurchaseMore arrived = msg.getArrived(i);

            // check if this is an old message:
            int seqNum = (int)(arrived.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"APurchaseMore arrived\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            int whid = arrived.getWhnum();

            String prompt = "";
            prompt += seqNum + ":APurchaseMore arrived:";
            int thingsCount = arrived.getThingsCount();
            for (int j = 0; j < thingsCount; j++) {

                WorldAmazon.AProduct productSet = arrived.getThings(j);
                int productID = (int)productSet.getId();
                int productCount = productSet.getCount();

                String singlePrompt = "(" + productSet.getDescription() + "," + productCount  +")";
                prompt += singlePrompt;

                // update warehose
                try {
                    this.backingService.updateStock(whid, productID, productCount);
                    Logger.logSys("Updated warehouse with whid " + whid + ", new products:" + singlePrompt);
                } catch (NoSuchWarehouseException e) {
                    Logger.logErr("Failed to update warehouse with whid " + whid + ":" + e.getMsg());
                }
            }
            Logger.logSys(prompt);
        }
    }

    //  repeated APacked ready = 2;
    protected void processAPacked(WorldAmazon.AResponses msg,
                                  CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getReadyCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to World: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getReady(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        WorldAmazon.ACommands aCommands = WorldAmazon.ACommands.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aCommands);
        this.threadPool.submit(ackSendingTask);

        // step3: check each "APacked ready" message, skip if is old seqnum; else continue to send ups AGetTruck
        for (int i = 0; i < count; i++) {
            WorldAmazon.APacked ready = msg.getReady(i);

            // check if this is an old message:
            int seqNum = (int)(ready.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"APacked ready\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            int shipid = (int)ready.getShipid();

            String prompt = "";
            prompt += seqNum + ":received APacked ready from World for packageID:" + shipid;
            Logger.logSys(prompt);

            // send AGetTruck to UPS for this package
            try {
                Package pkg = this.shoppingService.getPackage(shipid);
                // update package status as "Packed, Awaiting Truck Pickup"
                pkg.setStatus("Packed, Awaiting Truck Pickup");
                this.pkgDao.save(pkg);

                Logger.logSys("Sending UPS AGetTruck for packageID:" + pkg.getShipid() + "...");
                this.daemonService.getTruck(pkg);
                Logger.logSys("Finished Sending UPS AGetTruck for packageID:" + pkg.getShipid() + "!");
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());    // trust!!!
            }
        }
    }


    //  repeated ALoaded loaded = 3;
    protected void processedALoaded (WorldAmazon.AResponses msg,
                                     CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getLoadedCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to World: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getLoaded(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        WorldAmazon.ACommands aCommands = WorldAmazon.ACommands.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aCommands);
        this.threadPool.submit(ackSendingTask);

        // step3: check each "ALoaded loaded" message, skip if is old seqnum;
        // else continue to send ups ADeliver
        for (int i = 0; i < count; i++) {
            WorldAmazon.ALoaded loaded = msg.getLoaded(i);

            // check if this is an old message:
            int seqNum = (int)(loaded.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"ALoaded loaded\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            int shipID = (int)loaded.getShipid();
            String prompt = "";
            prompt += seqNum + ":received ALoaded from World for packageID:" + shipID;
            Logger.logSys(prompt);

            try {
                Package pkg = this.shoppingService.getPackage(shipID);
                Logger.logMsg("Sending \"ADeliver\" to UPS for packageID:" + shipID + "...");
                this.daemonService.deliver(pkg);
                Logger.logMsg("Finished Sending \"ADeliver\" to UPS for packageID:" + shipID + "!");

                // set package status as "Package on its way"
                pkg.setStatus("Package on its way");
                this.pkgDao.save(pkg);
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());  // trust!!
            }
        }

    }

    // repeated int64 acks = 6;
    protected void processAcks(WorldAmazon.AResponses msg,
                               DaemonService.AckWaitList ackWaitList) {
        int count = msg.getAcksCount();
        for (int i = 0; i < count; i++) {
            int seqNum = (int)msg.getAcks(i);
            Logger.logSys("Received ACK:"+ seqNum);

            // notify seqNum is ACK'd
            Notifier notifier = ackWaitList.getNotifier(seqNum);
            synchronized (notifier) {
                Logger.logSys("Notifying ACK:"+ seqNum);
                notifier.setSatisfied();
                notifier.notifyAll();
                Logger.logSys("Finished Notifying ACK:"+ seqNum);
            }
        }
    }

    // repeated APackage packagestatus = 7;
    public void processAPackage(WorldAmazon.AResponses msg,
                                CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getPackagestatusCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to World: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getPackagestatus(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        WorldAmazon.ACommands aCommands = WorldAmazon.ACommands.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aCommands);
        this.threadPool.submit(ackSendingTask);


        // step3: check each "APackage packagestatus" message, skip if is old seqnum; update related WareHose stock info
        for (int i = 0; i < count; i++) {
            WorldAmazon.APackage pkgStatus = msg.getPackagestatus(i);

            // check if this is an old message:
            int seqNum = (int) (pkgStatus.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"APackage packagestatus\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            int packageID = (int)pkgStatus.getPackageid();

            String prompt = "";
            prompt += seqNum + ":\"APackage packagestatus\" arrived for PackageID:" + packageID;
            Logger.logSys(prompt);

            try {
                Package pkg = this.shoppingService.getPackage(packageID);
                String updatePrompt = "Updating status for ";
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());   // trust!!
            }
        }
    }
}
