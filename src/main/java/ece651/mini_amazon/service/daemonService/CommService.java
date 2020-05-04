package ece651.mini_amazon.service.daemonService;

import com.google.protobuf.GeneratedMessageLite;
import ece651.mini_amazon.dao.WareHouseDao;
import ece651.mini_amazon.exceptions.commException.ConnectionException;
import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import ece651.mini_amazon.model.WareHouse;
import ece651.mini_amazon.utils.commChannel.CommChannel;
import ece651.mini_amazon.utils.commChannel.UPSChannel;
import ece651.mini_amazon.utils.commChannel.WorldChannel;
import ece651.mini_amazon.utils.concurrentTools.Notifier;
import ece651.mini_amazon.utils.tools.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import protobuf_generated.UpsAmazon;
import protobuf_generated.WorldAmazon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CommService {
    protected String upsIP;
    protected int upsPort;
    protected String worldIP;
    protected int worldPort;
    protected String localBindIP;
    protected int localBindPort;

    protected final Notifier notifier; // to notify the ServerInitializer that worldID has been set

    @Autowired
    WareHouseDao whDao;
    @Autowired
    SeqNumService seqNumService;

    protected UPSChannel upsChannel;
    protected WorldChannel worldChannel;
    protected Integer worldID;

    int ackWaitSeconds;
    protected ExecutorService commThreadPool;


    public CommService() {
        String str = "==> Created: CommService";
        Logger.logSys(str);
        this.worldID  = null;
        this.notifier = new Notifier();
    }

    public Notifier getNotifier() {
        return this.notifier;
    }
    public Integer getWorldID() {
        return this.worldID;
    }


    public void init(String upsIP, int upsPort,
                     String worldIP, int worldPort,
                     String localBindIP, int localBindPort,
                     int ackWaitSeconds) {

        String str1 = "==>Initializing CommService...";
        Logger.logSys(str1);

        // field init
        this.ackWaitSeconds = ackWaitSeconds;
        this.commThreadPool = Executors.newCachedThreadPool();
        this.upsIP = upsIP;
        this.upsPort = upsPort;
        this.worldIP = worldIP;
        this.worldPort = worldPort;
        this.localBindIP = localBindIP;
        this.localBindPort = localBindPort;

        String str2 = "==>Finished Initializing CommService!";
        Logger.logSys(str2);
    }

    public void initUPSConnection() throws ConnectionException {
        Logger.logSys("==>Initializing Socket Connection with UPS");
        ServerSocketChannel ssc;
        Logger.logSys("=====>Opening a ServerSocketChannel for UPS to connect...");
        try {
            ssc = ServerSocketChannel.open();
        } catch (IOException e) {
            String errStr = "ERROR: Cannot open a ServerSocketChannel, failed to connect to UPS";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Finish opening a ServerSocketChannel for UPS to connect!");

        Logger.logSys("=====>Binding ServerSocketChannel to Local Address:" + this.localBindIP + ":" + this.localBindPort + "...");
        try {
            ssc.bind(new InetSocketAddress(this.localBindIP, this.localBindPort));
        } catch (IOException e) {
            String errStr = "=====>IOException when Binding ServerSocketChannel to Local Address:" + this.localBindIP + ":" + this.localBindPort;
            errStr += " failed to connect to UPS";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Finished Binding ServerSocketChannel to Local Address:" + this.localBindIP + ":" + this.localBindPort + "!");

        SocketChannel sc;
        Logger.logSys("=====>Listening for UPS connection request...");
        try {
            sc = ssc.accept();
        } catch (IOException e) {
            String errStr = "ERROR: Cannot accept UPS connection request, aborting";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Connected with UPS!");

        Logger.logSys("=====>Wrapping the SocketChannel to UPS into UPSCommChannel...");
        try {
            this.upsChannel = new UPSChannel(sc, this.upsIP, this.upsPort);
        } catch (IOException e) {
            String errStr = "ERROR: FAILED Wrapping the SocketChannel to UPS into UPSCommChannel!";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Finished Wrapping the SocketChannel to UPS into UPSCommChannel!");

        Logger.logSys("==>Finished Socket Connection with UPS");
    }

    public void initWorldConnection() throws ConnectionException{
        Logger.logSys("==>Initializing Socket Connection with World");
        SocketChannel sc;
        Logger.logSys("=====>Opening a SocketChannel to connect to World...");
        try {
            sc = SocketChannel.open();
        } catch (IOException e) {
            String errStr = "Cannot open a SocketChannel, failed to connect to World";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Finished Opening a SocketChannel to connect to World...");

        Logger.logSys("=====>Connecting to world address " + this.worldIP + ":" + this.worldPort);
        try {
            sc.connect(new InetSocketAddress(this.worldIP, this.worldPort));
        } catch (IOException e) {
            String errStr = "Cannot connect to world address " +this.worldIP + ":" + this.worldPort;
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Connected with World!");

        Logger.logSys("=====>Wrapping the SocketChannel to World into WorldCommChannel...");
        try {
            this.worldChannel = new WorldChannel(sc, this.worldIP, this.worldPort);
        } catch (IOException e) {
            String errStr = "FAILED Wrapping the SocketChannel to World into WorldCommChannel!";
            throw new ConnectionException(errStr);
        }
        Logger.logSys("=====>Finished Wrapping the SocketChannel to World into WorldCommChannel!");

        Logger.logSys("==>Finished Socket Connection with World");
    }

    public void confirmWorldConnection() throws DisconnectionException{
        // add warehouse infos to init
        List<WareHouse> whList = (List<WareHouse>)this.whDao.findAll();
        List<WorldAmazon.AInitWarehouse> aInitWarehouses = new ArrayList<>();
        for (WareHouse wh : whList) {
            int id = wh.getWhid();
            int x = wh.getX();
            int y = wh.getY();
            WorldAmazon.AInitWarehouse aInitWarehouse = WorldAmazon.AInitWarehouse.newBuilder().setId(id).setX(x).setY(y).build();
            aInitWarehouses.add(aInitWarehouse);
        }

        // send AConnect
        WorldAmazon.AConnect aConnect;
        if (this.worldID == null) {   // this only happens when you want to test without connection to ups
            aConnect = WorldAmazon.AConnect.newBuilder().addAllInitwh(aInitWarehouses).setIsAmazon(true).build();
        } else {
            aConnect = WorldAmazon.AConnect.newBuilder().setWorldid(this.worldID).addAllInitwh(aInitWarehouses).setIsAmazon(true).build();
        }
        this.worldChannel.send(aConnect);

        String sentMsg = "======>Sent AConnect: worldid:" + this.worldID;
        Logger.logSys(sentMsg);
        // receive AConnected
        try {
            WorldAmazon.AConnected aConnected = WorldAmazon.AConnected.parseDelimitedFrom(this.worldChannel.getSocket().getInputStream());
            int rcvdWorldID = (int)aConnected.getWorldid();
            String result = aConnected.getResult();

            String connectedMsg = "======>Received AConnected: worldid:" + rcvdWorldID + " result:"+result;
            Logger.logSys(connectedMsg);

        } catch (IOException e) {
            throw new DisconnectionException("IOException during attempt to connect receive AConnected world");
        }
    }


    // guranteed send: used to send all messages except for ACKs;
    public void guaranteedSend(Notifier notifier,
                               String channelType,
                               int seqNum,
                               GeneratedMessageLite<?, ?> msg) {

        CommChannel channel;
        if (channelType.equals("ups")) {
            channel = this.upsChannel;
        } else if (channelType.equals("world")) {
            channel = this.worldChannel;
        } else {
            Logger.logErr(seqNum + ":ERROR: unrecognized channelType \""+ channelType + "\" specified, aborting...");
            return;
        }
        GuaranteedSendTask task = new GuaranteedSendTask(this.ackWaitSeconds, notifier, channel, seqNum, msg);

        // submit sending-task to thread pool
        this.commThreadPool.submit(task);
    }

    protected UpsAmazon.UMessages receiveUMessage() throws DisconnectionException {
        UpsAmazon.UMessages msg = this.upsChannel.recv();
        return msg;
    }

    protected WorldAmazon.AResponses receiveAResponse() throws DisconnectionException {
        WorldAmazon.AResponses msg = this.worldChannel.recv();
        return msg;
    }

    protected class GuaranteedSendTask extends Thread{
        int seqNum;
        CommChannel channel;
        int waitSeconds;
        final Notifier notifier;
        GeneratedMessageLite<?, ?> msg;

        public GuaranteedSendTask(int waitSeconds,
                              Notifier notifier,
                              CommChannel channel,
                              int seqNum,
                              GeneratedMessageLite<?, ?> msg) {
            this.waitSeconds = waitSeconds;
            this.seqNum = seqNum;
            this.notifier = notifier;
            this.channel = channel;
            this.msg = msg;
        }

        public void run () {
            // send repeatedly, until received notify of ACK
            while (true) {
                try {
                    channel.send(msg);
                    Logger.logSys(this.seqNum + ":Message sent!");
                } catch (DisconnectionException e) {
                    Logger.logErr(this.seqNum + ":ERROR during attempt to send a message via threadPool: " + e.getMsg() + ", aborting...");
                    break;
                }
                synchronized (this.notifier) {
                    try {
                        this.notifier.wait(this.waitSeconds * 1000);
                        if (this.notifier.isSatisfied()) {
                            Logger.logSys(this.seqNum + ":ACK Notification Received!");
                            break;
                        }
                    } catch (InterruptedException e) {
                        Logger.logErr(this.seqNum + ":Interrupt happened to this thread!");
                    }
                }
                Logger.logSys(this.seqNum + ":No ACK received in " + this.waitSeconds + " seconds, re-sending...");
            }
        }
    }
}
