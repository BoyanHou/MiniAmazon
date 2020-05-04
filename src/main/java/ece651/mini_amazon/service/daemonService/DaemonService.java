package ece651.mini_amazon.service.daemonService;

import ece651.mini_amazon.dao.PackageDao;
import ece651.mini_amazon.dao.ProductDao;
import ece651.mini_amazon.dao.WareHouseDao;
import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchPackageException;
import ece651.mini_amazon.model.Product;
import ece651.mini_amazon.model.Package;
import ece651.mini_amazon.service.webService.ShoppingService;
import ece651.mini_amazon.utils.concurrentTools.Notifier;
import ece651.mini_amazon.utils.tools.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import protobuf_generated.UpsAmazon;
import protobuf_generated.WorldAmazon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DaemonService {
    @Autowired
    CommService commService;
    @Autowired
    UMessageProcessingService uMessageProcessingService;
    @Autowired
    WMessageProcessingService wMessageProcessingService;
    @Autowired
    ProductDao productDao;
    @Autowired
    SeqNumService seqNumService;
    @Autowired
    WareHouseDao wareHouseDao;
    @Autowired
    PackageDao pkgDao;
    @Autowired
    ShoppingService shoppingService;

    protected ExecutorService threadPool;
    protected AckWaitList ackWaitList;


    public DaemonService() {
        this.threadPool = Executors.newCachedThreadPool();
        this.ackWaitList = new AckWaitList();
        String str = "==>Created: DaemonService";
        Logger.logSys(str);
    }


    public void pack(int whnum, JSONObject items, int shipid) {
        // get sequence number
        int seqNum = this.seqNumService.getSeqNum();

        // make List<AProduct> --> APack --> ACommands
        List<WorldAmazon.AProduct> productList = this.makeAProductList(items);
        WorldAmazon.APack aPack = WorldAmazon.APack.newBuilder().
                setWhnum(whnum).
                addAllThings(productList).
                setShipid(shipid).
                setSeqnum(seqNum).build();
        //        message APack{
//            required int32 whnum = 1;
//            repeated AProduct things = 2;
//            required int64 shipid = 3;
//            required int64 seqnum = 4;
//        }
        WorldAmazon.ACommands aCommand = WorldAmazon.ACommands.newBuilder().addTopack(aPack).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "world", seqNum, aCommand);

        // update package status as "Packing at WareHouse"
        Package pkg;
        try {
            pkg = this.shoppingService.getPackage(shipid);
            pkg.setStatus("Packing at WareHouse");
            this.pkgDao.save(pkg);
        } catch (NoSuchPackageException e) {
            Logger.logErr(e.getMsg());
        }

    }

    // send UMessage:AInitialWorldid to ups, to query the "this.worldid" field
    public void getInitialWorldIDForCommService() {
        // get seqNum
        int seqNum = this.seqNumService.getSeqNum();

        Logger.logSys("=====> Making AInitialWorldid(" + seqNum + ") to be sent to UPS...");
        // make AInitialWorldid --> AMessages
        UpsAmazon.AInitialWorldid aInitialWorldid = UpsAmazon.AInitialWorldid.newBuilder().setSeqnum(seqNum).build();
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().setInitialWorldid(aInitialWorldid).build();
        Logger.logSys("=====> Finished Making AInitialWorldid(" + seqNum + ")  to be sent to UPS!");

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send to ups using commService
        this.commService.guaranteedSend(notifier, "ups", seqNum, aMessage);
    }

    public void deliver(Package pkg) {
        // get sequence number
        int seqNum = seqNumService.getSeqNum();

//        message ADeliver {
//            required int32 truckid = 1;
//            required int64 seqnum = 2;
//            required int64 worldid = 3;
//        }

        // make ADeliver --> AMessages
        UpsAmazon.ADeliver aDeliver = UpsAmazon.ADeliver.newBuilder().
                setTruckid(pkg.getTruckID()).   //  required int32 truckid = 1;
                setSeqnum(seqNum).              //  required int64 seqnum = 3;
                setWorldid(this.commService.getWorldID()).build();
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().addDelivers(aDeliver).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "ups", seqNum, aMessage);
    }

    public void queryPackageStatus(Package pkg) {
        // get sequence number
        int seqNum = seqNumService.getSeqNum();

//        message AQuery{
//            required int64 packageid = 1;
//            required int64 seqnum = 2;
//        }

        // make AQuery --> ACommands
        WorldAmazon.AQuery aQuery = WorldAmazon.AQuery.newBuilder().
                setPackageid(pkg.getShipid()).     //  required int64 packageid = 1;
                setSeqnum(seqNum).build();         //  required int64 seqnum = 2;
        WorldAmazon.ACommands aCommand = WorldAmazon.ACommands.newBuilder().addQueries(aQuery).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "world", seqNum, aCommand);
    }


    public void getTruck(Package pkg) {
        // get sequence number
        int seqNum = seqNumService.getSeqNum();

        //        message AGetTruck {
//            required int32 whid = 1;
//            required int64 packageid = 2;
//            optional string uAccountName = 3;
//            required int32 x = 4;
//            required int32 y = 5;
//            repeated AmazonProduct product = 6;
//            required int64 seqnum = 7;
//            required int64 worldid = 8;
//        }

        // make List<AmazonProduct> --> AGetTruck --> AMessages
        JSONObject itemsJO = new JSONObject(pkg.getProducts());
        List<UpsAmazon.AmazonProduct> amazonProductList = this.makeAmazonProductList(itemsJO);

        UpsAmazon.AGetTruck aGetTruck;
        if (pkg.getuAccountName().length() != 0) {
            aGetTruck = UpsAmazon.AGetTruck.newBuilder().
                    setWhid(pkg.getWh().getWhid()).          //  required int32 whid = 1;
                    setPackageid(pkg.getShipid()).           //  required int64 packageid = 2;
                    setUAccountName(pkg.getuAccountName()).  //  optional string uAccountName = 3;
                    setX(pkg.getTargetX()).                  //  required int32 x = 4;
                    setY(pkg.getTargetY()).                  //  required int32 y = 5;
                    addAllProduct(amazonProductList).        //  repeated AmazonProduct product = 6;
                    setSeqnum(seqNum).                       //  required int64 seqnum = 7;
                    setWorldid(this.commService.getWorldID()).build(); // required int64 worldid = 8;
        } else {
            aGetTruck = UpsAmazon.AGetTruck.newBuilder().
                    setWhid(pkg.getWh().getWhid()).          //  required int32 whid = 1;
                    setPackageid(pkg.getShipid()).           //  required int64 packageid = 2;
                    setX(pkg.getTargetX()).                  //  required int32 x = 4;
                    setY(pkg.getTargetY()).                  //  required int32 y = 5;
                    addAllProduct(amazonProductList).        //  repeated AmazonProduct product = 6;
                    setSeqnum(seqNum).                       //  required int64 seqnum = 7;
                    setWorldid(this.commService.getWorldID()).build(); // required int64 worldid = 8;
        }
        UpsAmazon.AMessages aMessage = UpsAmazon.AMessages.newBuilder().addGetTrucks(aGetTruck).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "ups", seqNum, aMessage);
    }

    public void putOnTruck(Package pkg, int truckID) {
        // get sequence number
        int seqNum = seqNumService.getSeqNum();

//        message APutOnTruck{
//            required int32 whnum = 1;
//            required int32 truckid = 2;
//            required int64 shipid = 3;
//            required int64 seqnum = 4;
//        }

        // APutOnTruck --> ACommands
        WorldAmazon.APutOnTruck aPutOnTruck = WorldAmazon.APutOnTruck.newBuilder().
                setWhnum(pkg.getWh().getWhid()).   //  required int32 whnum = 1;
                setTruckid(truckID).               //  required int32 truckid = 2;
                setShipid(pkg.getShipid()).        //  required int64 shipid = 3;
                setSeqnum(seqNum).build();         //  required int64 seqnum = 4;
        WorldAmazon.ACommands aCommand = WorldAmazon.ACommands.newBuilder().addLoad(aPutOnTruck).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "world", seqNum, aCommand);
    }


    public void purchaseMore(JSONObject items, int whID) {
        // get sequence number
        int seqNum = seqNumService.getSeqNum();

        // make List<AProduct> --> APurchaseMore --> ACommands
        List<WorldAmazon.AProduct> productList = this.makeAProductList(items);
        WorldAmazon.APurchaseMore aPurchaseMore = WorldAmazon.APurchaseMore.newBuilder().
                setSeqnum(seqNum).addAllThings(productList).setWhnum(whID).build();
        WorldAmazon.ACommands aCommand = WorldAmazon.ACommands.newBuilder().addBuy(aPurchaseMore).build();

        // register in ACK List
        Notifier notifier = new Notifier();
        this.ackWaitList.register(seqNum, notifier);

        // send using commService
        this.commService.guaranteedSend(notifier, "world", seqNum, aCommand);
    }

    protected class AckWaitList {
        protected HashMap<Integer, Notifier> notifierMap;
        protected Lock lock;
        public AckWaitList() {
            this.notifierMap = new HashMap<>();
            this.lock = new ReentrantLock();
        }

        // register a seqNum as "waiting to be ACK'ed"
        public void register(int seqNum, Notifier notifier) {
            this.lock.lock();
            this.notifierMap.put(seqNum, notifier);
            this.lock.unlock();
        }

        public Notifier getNotifier(int seqNum) {
            return this.notifierMap.get(seqNum);
        }

        // notify a seqnum has been ACK'ed
        public void notify(int seqNum) {
            this.lock.lock();
            if (this.notifierMap.containsKey(seqNum)) {
                Notifier notifier = this.notifierMap.get(seqNum);
                synchronized (notifier) {
                    notifier.setSatisfied();
                    notifier.notifyAll();
                }
                this.notifierMap.remove(seqNum);
            }
            this.lock.unlock();
        }
    }

    public void runWorldReceiverDaemon () {
        Logger.logSys("=====>Launching World Receiver Daemon...");
        WorldReceiverDaemon daemon = new WorldReceiverDaemon(this.commService, this.wMessageProcessingService, this.ackWaitList);
        this.threadPool.submit(daemon);
        Logger.logSys("=====>Finished Launching World Receiver Daemon!");
    }

    protected class WorldReceiverDaemon extends Thread {
        CommService commService;
        WMessageProcessingService processingService;
        AckWaitList ackWaitList;

        public WorldReceiverDaemon (CommService commService,
                                  WMessageProcessingService processingService,
                                  AckWaitList ackWaitList) {
            this.commService = commService;
            this.processingService = processingService;
            this.ackWaitList = ackWaitList;
        }

        public void run() {
            while(true) {
                WorldAmazon.AResponses msg;
                try {
                    // receive msg
                    msg = this.commService.receiveAResponse();

                    // process msg
                    this.processingService.process(msg,
                            this.commService.worldChannel,
                            this.ackWaitList);
                } catch (DisconnectionException e) {
                    Logger.logErr("==>ERROR: World Receiver Daemon Stopped due to Disconnection with Amazon: " + e.getMsg());
                    break;
                }
            }
        }
    }

    public void runUpsReceiverDaemon () {
        Logger.logSys("=====>Launching UPS Receiver Daemon...");
        UPSReceiverDaemon daemon = new UPSReceiverDaemon(this.commService, this.uMessageProcessingService, this.ackWaitList);
        this.threadPool.submit(daemon);
        Logger.logSys("=====>Finished Launching UPS Receiver Daemon!");
    }

    protected class UPSReceiverDaemon extends Thread {
        CommService commService;
        UMessageProcessingService processingService;
        AckWaitList ackWaitList;

        public UPSReceiverDaemon (CommService commService,
                                  UMessageProcessingService processingService,
                                  AckWaitList ackWaitList) {
            this.commService = commService;
            this.processingService = processingService;
            this.ackWaitList = ackWaitList;
        }

        public void run() {
            while(true) {
                UpsAmazon.UMessages uMessage;
                try {
                    uMessage = this.commService.receiveUMessage();     // receive
                    this.processingService.process(uMessage,           // process
                            this.commService.upsChannel,
                            this.ackWaitList);
                } catch (DisconnectionException e) {
                    Logger.logErr("==>ERROR: UPS Receiver Daemon Stopped due to Disconnection with UPS: " + e.getMsg());
                    break;
                }
            }
        }
    }

    public List<WorldAmazon.AProduct> makeAProductList (JSONObject items) {
        List<WorldAmazon.AProduct> productList = new ArrayList<>();
        for (String productIDStr : items.keySet()) {
            int productID = Integer.parseInt(productIDStr);

            int productNum = items.getInt(productIDStr);
            String description = "";

            Product product = productDao.findById(productID).orElse(null);
            if (product != null) { // faith!!
                description = product.getDescription();
            }

            WorldAmazon.AProduct aProduct = WorldAmazon.AProduct.newBuilder().
                    setId(productID).
                    setCount(productNum).
                    setDescription(description).build();
            productList.add(aProduct);
        }
        return productList;
    }

    public List<UpsAmazon.AmazonProduct> makeAmazonProductList (JSONObject items) {
        List<UpsAmazon.AmazonProduct> productList = new ArrayList<>();
        for (String productIDStr : items.keySet()) {
            int productID = Integer.parseInt(productIDStr);

            int productNum = items.getInt(productIDStr);
            String description = "";

            Product product = productDao.findById(productID).orElse(null);
            if (product != null) { // faith!!
                description = product.getDescription();
            } else {
                Logger.logErr("No such AmazonProduct with id:" + productID);
            }

            UpsAmazon.AmazonProduct amazonProduct = UpsAmazon.AmazonProduct.newBuilder().
                    setProductid(productID).
                    setCount(productNum).
                    setDescription(description).build();
            productList.add(amazonProduct);
        }
        return productList;
    }


}
