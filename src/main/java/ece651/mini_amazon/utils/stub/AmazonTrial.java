package ece651.mini_amazon.utils.stub;

import ece651.mini_amazon.utils.commChannel.WorldChannel;
import protobuf_generated.WorldAmazon;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class AmazonTrial {
    public static void main(String[] args) throws Exception {
        String worldIP = "vcm-14419.vm.duke.edu";
        WorldAmazon.ACommands aCommands;
        WorldAmazon.AResponses aResponses;

        int worldPort = 23456;

        WorldChannel worldChannel;
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(worldIP, worldPort));

        WorldAmazon.AInitWarehouse wh = WorldAmazon.AInitWarehouse.newBuilder().setId(1).setX(10).setY(20).build();
        WorldAmazon.AConnect aConnect = WorldAmazon.AConnect.newBuilder().addInitwh(wh).setIsAmazon(true).build();

        worldChannel = new WorldChannel(sc, worldIP, worldPort);
        worldChannel.send(aConnect);

        WorldAmazon.AConnected aConnected = WorldAmazon.AConnected.parseDelimitedFrom(sc.socket().getInputStream());
        String str = aConnected.toString();

        System.out.println(str);

        // purchase more
        WorldAmazon.AProduct apples = WorldAmazon.AProduct.newBuilder().setId(1).setDescription("apple").setCount(3).build();
        WorldAmazon.AProduct pears = WorldAmazon.AProduct.newBuilder().setId(2).setDescription("pear").setCount(3).build();
        List<WorldAmazon.AProduct> products = new ArrayList<>();
        products.add(apples);
        products.add(pears);
        WorldAmazon.APurchaseMore aPurchaseMore = WorldAmazon.APurchaseMore.newBuilder().setWhnum(1).setSeqnum(1).addAllThings(products).build();
        aCommands = WorldAmazon.ACommands.newBuilder().addBuy(aPurchaseMore).build();

        worldChannel.send(aCommands);
        aResponses = worldChannel.recv();
        int arrviedCount = aResponses.getArrivedCount();
        for (int i = 0; i < arrviedCount; i++) {
            WorldAmazon.APurchaseMore arrived = aResponses.getArrived(i);

            // let world pack
            List<WorldAmazon.AProduct> productList = arrived.getThingsList();
            WorldAmazon.APack aPack = WorldAmazon.APack.newBuilder().addAllThings(productList).setSeqnum(2).setShipid(2).setWhnum(1).build();
            aCommands = WorldAmazon.ACommands.newBuilder().addTopack(aPack).build();

            worldChannel.send(aCommands);

            String arrivedStr = arrived.toString();
            System.out.println(arrivedStr);
        }

        for  (int i = 0; i < 3; i ++) {
            aResponses = worldChannel.recv();
        }

//        WorldAmazon.APacked aPacked = aResponses.getReady(0);
//        String aPackedStr = aPacked.toString();
//        System.out.println(aPackedStr);
//
//        WorldAmazon.APurchaseMore arrived = aResponses.getArrived(0);
//        String arrivedStr = arrived.toString();
//        System.out.println(arrivedStr);


        System.out.println("Amazon Trial Ended");

    }
}
