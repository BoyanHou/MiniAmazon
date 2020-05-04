package ece651.mini_amazon.service.daemonService;

import com.google.protobuf.GeneratedMessageLite;
import ece651.mini_amazon.dao.PackageDao;
import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchPackageException;
import ece651.mini_amazon.service.webService.ShoppingService;
import ece651.mini_amazon.utils.commChannel.CommChannel;
import ece651.mini_amazon.model.Package;
import ece651.mini_amazon.utils.concurrentTools.Notifier;
import ece651.mini_amazon.utils.tools.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import protobuf_generated.UpsAmazon;
import protobuf_generated.WorldAmazon;

import java.util.ArrayList;
import java.util.List;

@Service
public class UMessageProcessingService extends MessageProcessingService {
    @Autowired
    DaemonService daemonService;
    @Autowired
    CommService commService;
    @Autowired
    ShoppingService shoppingService;
    @Autowired
    PackageDao packageDao;


    public void process(UpsAmazon.UMessages msg,
                        CommChannel commChannel,
                        DaemonService.AckWaitList ackWaitList) {
//            repeated int64 acks = 1;
        this.processAcks(msg, ackWaitList);
//            optional UInitialWorldid initialWorldid = 2;
        this.processUInitialWorldid(msg, commChannel);
//        optional UAccountResult accountResult = 3;
        this.processUAccountResult(msg, commChannel);
//            repeated UTruckReady truckReadies = 4;
        this.processUTruckReady(msg, commChannel);
//        repeated UPackageDelivered deliveredpackages = 16;
        this.processUPackageDelivered(msg, commChannel);

//            repeated UAccountPackageQuery uAccountPackageQueries = 5;
//            repeated UAccountConnectionResult accountconnectionresult = 6;



//            optional UDisconnect disconnect = 8;
//            optional UDisconnectWorld disconnectWorld = 9;
//            optional UConnectWorld connectWorld = 10;
//            repeated UDisconnectAccount disconnectAccounts = 11;
//            repeated UDisconnectPackage disconnectPackages = 12;
//            optional UWorldidSummaryReply worldidSummaryReply = 13;
//            optional UMadeWorld madeWorld = 14;
//            repeated UPackageCoord packageCoords = 15;

    }

    // repeated int64 acks = 1;
    protected void processAcks(UpsAmazon.UMessages msg,
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

    //  optional UInitialWorldid initialWorldid = 2;
    protected void processUInitialWorldid(UpsAmazon.UMessages msg,
                                   CommChannel commChannel) {
        // step1: check if this optional field exists
        if (!msg.hasInitialWorldid()) {
            return;
        }
        UpsAmazon.UInitialWorldid uInitialWorldid = msg.getInitialWorldid();

        // step2: send back ack
        String ackPromt = "Ack Collection to send UPS: ";
        Long ack = uInitialWorldid.getSeqnum();
        ackPromt += ack;
        Logger.logSys(ackPromt);
        UpsAmazon.AMessages uMessage = UpsAmazon.AMessages.newBuilder().addAcks(ack).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, uMessage);
        this.threadPool.submit(ackSendingTask);

        // step3: check "UInitialWorldid initialWorldid" message, skip if is old seqnum; else: set CommService's worldID field & notify its notifier
        int seqNum = (int)(uInitialWorldid.getSeqnum());
        if (this.processedSeqnums.contains(seqNum)) {
            Logger.logSys("Found old seqNum:" + seqNum + " (\"UInitialWorldid initialWorldid\"), skip processing this one");
            return;
        } else {
            this.processedSeqnums.add(seqNum);
        }

        // get & set worldID, then notify CommService
        int worldID = (int)uInitialWorldid.getWorldid(0);
        Logger.logSys("======>" + seqNum + ":Received worldID from UPS:" + worldID);
        synchronized (commService.notifier) {
            commService.worldID = worldID;
            Logger.logSys("======>Notifying CommService that worldID has been set to " + worldID + "...");
            commService.notifier.notifyAll();
            Logger.logSys("======>Finished Notifying CommService that worldID has been set to " + worldID + "!");
        }
    }

//        optional UAccountResult accountResult = 3;
    protected void processUAccountResult (UpsAmazon.UMessages msg,
                                          CommChannel commChannel) {
        // step1: check if this optional field exists
        if (!msg.hasAccountResult()) {
            return;
        }
        UpsAmazon.UAccountResult uAccountResult = msg.getAccountResult();

        // step2: send back ack
        String ackPromt = "Ack Collection to send UPS: ";
        Long ack = uAccountResult.getSeqnum();
        ackPromt += ack;
        Logger.logSys(ackPromt);
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().addAcks(ack).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aMessage);
        this.threadPool.submit(ackSendingTask);

        // step3: check "UInitialWorldid initialWorldid" message, skip if is old seqnum; else: set CommService's worldID field & notify its notifier
        int seqNum = (int)(uAccountResult.getSeqnum());
        if (this.processedSeqnums.contains(seqNum)) {
            Logger.logSys("Found old seqNum:" + seqNum + " (\"UAccountResult accountResult\"), skip processing this one");
            return;
        } else {
            this.processedSeqnums.add(seqNum);
        }

        Logger.logSys("" + seqNum + ":Received (\"UAccountResult accountResult\") from UPS!");

        int packageID = (int)uAccountResult.getPackageid();

        if (!uAccountResult.getUAccountExists()) { // account does not exist
            Logger.logMsg("UPS tells: Currently No UPS Account exists with name " + uAccountResult.getUAccountName() + "!");
            try {
                Package pkg = shoppingService.getPackage(packageID);
                // update to prompt user that UPS account name does not exist
                String invalidUPSAccoutNameMessage = "Sorry, your entered UPS account name: " + pkg.getuAccountName() + "does not exists!";
                pkg.setuAccountName(invalidUPSAccoutNameMessage);
                this.packageDao.save(pkg);
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());  // faith !!
            }
            return;
        }

        try {
            Package pkg = shoppingService.getPackage(packageID);
            int uid = (int)uAccountResult.getUAccountid();
            Logger.logSys("Updating packageID:" + packageID + " associated uid:" + uid + "...");
            pkg.setUid(uid);
            this.packageDao.save(pkg);
            Logger.logSys("Finished Updating packageID:" + packageID + " associated uid:" + uid + "!");

        } catch (NoSuchPackageException e) {
            Logger.logErr(e.getMsg());   // trust!!
        }
    }


//    repeated UTruckReady truckReadies = 4;
    protected void processUTruckReady(UpsAmazon.UMessages msg,
                                      CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getTruckReadiesCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to UPS: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getTruckReadies(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aMessage);
        this.threadPool.submit(ackSendingTask);

        // step3: check each "repeated UTruckReady truckReadies" message, skip if is old seqnum;
        // else continue to send world "APutOnTruck load"
        for (int i = 0; i < count; i++) {
            UpsAmazon.UTruckReady truckReady = msg.getTruckReadies(i);

            // check if this is an old message:
            int seqNum = (int)(truckReady.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"UTruckReady truckReadies\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            // get shipid & truckid from this message, then tell World to Load
            int shipID = (int)truckReady.getPackageid();
            int truckID = truckReady.getTruckid();

            Logger.logSys("" + seqNum + ": received UTruckReady from UPS for packageID:" + shipID);

            try {
                Package pkg = shoppingService.getPackage(shipID);
                Logger.logSys("Saving TruckID:" + truckID + " for packageID:" + shipID + "...");
                pkg.setTruckID(truckID);
                this.packageDao.save(pkg);
                Logger.logSys("Finished Saving TruckID:" + truckID + " for packageID:" + shipID + "!");

                Logger.logSys("Sending PutOnTruck to World for packageID:" + shipID + "...");
                this.daemonService.putOnTruck(pkg, truckID);
                Logger.logSys("Finished Sending PutOnTruck to World for packageID:" + shipID + "!");

                // set package status as "Truck arrived, Loading on Truck"
                pkg.setStatus("Truck arrived, Loading on Truck");
                this.packageDao.save(pkg);
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());
            }
        }
    }

    //repeated UPackageDelivered deliveredpackages = 16;
    protected void processUPackageDelivered (UpsAmazon.UMessages msg,
                                             CommChannel commChannel) {
        // step1: check if count is 0
        int count = msg.getDeliveredpackagesCount();
        if (count == 0) {
            return;
        }

        // step2: build ack list from seqnums, build msg from acks, send back acks
        List<Long> acks = new ArrayList<>();
        String ackPromt = "Ack Collection to send to UPS: ";
        for (int i = 0; i < count; i++) {
            Long seqNum = msg.getDeliveredpackages(i).getSeqnum();
            acks.add(seqNum);
            ackPromt += seqNum + ", ";
        }
        Logger.logSys(ackPromt);
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().addAllAcks(acks).build();
        AckSendingTask ackSendingTask = new AckSendingTask(commChannel, aMessage);
        this.threadPool.submit(ackSendingTask);

        // step3: check each "UPackageDelivered deliveredpackages" message, skip if is old seqnum;
        // else update the package's status as "Package Delivered"
        for (int i = 0; i < count; i++) {
            UpsAmazon.UPackageDelivered delivered = msg.getDeliveredpackages(i);

            // check if this is an old message:
            int seqNum = (int)(delivered.getSeqnum());
            if (this.processedSeqnums.contains(seqNum)) {
                Logger.logSys("Found old seqNum:" + seqNum + " (\"UPackageDelivered deliveredpackages\"), skip processing this one");
                continue;
            } else {
                this.processedSeqnums.add(seqNum);
            }

            // get shipid from this message, then update the status of that package
            int shipID = (int)delivered.getPackageid();

            Logger.logSys("" + seqNum + ": received UPackageDelivered from UPS for packageID:" + shipID);

            try {
                Package pkg = shoppingService.getPackage(shipID);
                // set package status as "Package Delivered"
                pkg.setStatus("Package Delivered");
                this.packageDao.save(pkg);
            } catch (NoSuchPackageException e) {
                Logger.logErr(e.getMsg());
            }
        }
    }
}

