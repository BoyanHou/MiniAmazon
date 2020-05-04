package ece651.mini_amazon.utils.stub;

import ece651.mini_amazon.utils.commChannel.WorldChannel;
import protobuf_generated.WorldUps;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class UPSStub {

    public static void main (String[]args) throws Exception {
        System.out.println("UPS STUB STARTED!");

        String worldIP = "vcm-14419.vm.duke.edu";
        int worldPort = 12345;
        UPSWorldChannelStub worldChannel;

        // socket connect to world
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(worldIP, worldPort));
        System.out.println("Connected!");

        worldChannel = new UPSWorldChannelStub(sc, worldIP, worldPort);

        // init world
        WorldUps.UInitTruck truck = WorldUps.UInitTruck.newBuilder().setId(1).setX(100).setY(250).build();
        WorldUps.UConnect uconnect = WorldUps.UConnect.newBuilder().setIsAmazon(false).addTrucks(truck).build();
        worldChannel.send(uconnect);

//        Thread.sleep(9000);
//        InputStream is = sc.socket().getInputStream();
//        String str = is.toString();
//        System.out.println("from world: "+str);

        for (int i = 0; i < 3; i++) {
            Thread.sleep(3000);
            WorldUps.UResponses resp= worldChannel.recv();
            System.out.println("Received World Msg:" + resp.toString());
        }
        return;

    }

}
