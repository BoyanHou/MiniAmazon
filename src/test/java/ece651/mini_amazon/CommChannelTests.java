//package ece651.mini_amazon;
//
//import ece651.mini_amazon.exceptions.MsgException;
//
//import ece651.mini_amazon.utils.commChannel.UPSChannel;
//import org.junit.jupiter.api.Test;
//import protobuf_generated.UpsAmazon;
//import protobuf_generated.WorldAmazon;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.StandardCharsets;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//
//public class CommChannelTests {
//    @Test
//    public void testProtoBuf() throws Exception {
//        WorldAmazon.AProduct product = WorldAmazon.AProduct.newBuilder().
//                setCount(3).
//                setDescription("heyAmazon").setId(250).build();
//
//        byte[] bytes = product.toByteArray();
//
//        WorldAmazon.AProduct productRecover = WorldAmazon.AProduct.parseFrom(bytes);
//
//        System.out.println(productRecover.toString());
//    }
//
//    @Test
//    public void testCommunication () throws Exception{
//        String byteMessage = "This is a byte message";
//        byte[] byteArray = byteMessage.getBytes();
//        String v = new String(byteArray, StandardCharsets.UTF_8);
//        System.out.println(v);
//        System.out.println("hhhh");
//        ExecutorService es = Executors.newCachedThreadPool();
//
//        //ServerSocketChannelThread ssct = new ServerSocketChannelThread(ServerSocketChannel.open(), 7000);
//        //ssct.start();
//        es.execute(new UPSServerSocketChannelThread(ServerSocketChannel.open(), 7000));
//        Thread.sleep(1000);
////        SocketChannelThread sct = new SocketChannelThread(SocketChannel.open(), 7000);
////        sct.start();
//        es.execute(new UPSSocketChannelThread(SocketChannel.open(), 7000));
////        ssct.join();
////        sct.join();
//        es.shutdown();
//        while (!es.awaitTermination(24L, TimeUnit.HOURS)) {
//            System.out.println("Not yet. Still waiting for termination");
//        }
//    }
//
//    protected class UPSSocketChannelThread extends Thread {
//        SocketChannel sc;
//        UPSChannel cc;
//        int port;
//        public UPSSocketChannelThread(SocketChannel sc, int port) {
//            System.out.println("SocketChannelThread Created");
//            this.sc = sc;
//            this.port = port;
//        }
//        @Override
//        public void run() {
//            System.out.println("SocketChannelThread Running");
//            try {
//                System.out.println("SocketChannelThread TRY");
//                this.sc.connect(new InetSocketAddress("0.0.0.0", this.port));
//                this.cc = new UPSChannel(this.sc, "0.0.0.0", this.port);
//                System.out.println("SocketChannelThread TRY END");
//            } catch (IOException e) {
//                System.out.println("ERROR: IOException during SocketChannel attempt to connect to local port:" + this.port);
//            }
//            System.out.println("SUCCESS: SocketChannel connected to local port:" + this.port);
//            // do some missions with Executor Service (thread pool)
//
//            ExecutorService es = Executors.newCachedThreadPool();
//
//
//            Future future1 = es.submit(new RecvUMessagesThread(this.cc));
//            es.execute(new SendUMessagesThread(this.cc, 1000));
//            es.execute(new SendUMessagesThread(this.cc, 2000));
//            es.execute(new SendUMessagesThread(this.cc, 3000));
//            es.shutdown();
//            try {
//                while (!es.awaitTermination(24L, TimeUnit.HOURS)) {
//                    System.out.println("Not yet. Still waiting for termination");
//                }
//            } catch (InterruptedException e) {
//                System.out.println("Interrupt Exception");
//            }
//        }
//    }
//
//    protected class UPSServerSocketChannelThread extends Thread {
//        ServerSocketChannel ssc;
//        SocketChannel sc;
//        UPSChannel cc;
//        int port;
//        public UPSServerSocketChannelThread(ServerSocketChannel ssc, int port) {
//            System.out.println("ServerSocketChannelThread Created");
//            this.ssc = ssc;
//            this.port = port;
//        }
//
//        @Override
//        public void run() {
//            System.out.println("ServerSocketChannelThread Running");
//            try {
//                this.ssc.bind(new InetSocketAddress(this.port));
//                this.sc = ssc.accept();
//                this.cc = new UPSChannel(sc, "0.0.0.0", port);
//
//                // do some missions with Executor Service (thread pool)
//                ExecutorService es = Executors.newCachedThreadPool();
//                es.execute(new RecvUMessagesThread(this.cc));
//
//                es.execute(new SendUMessagesThread(this.cc, 100));
//                es.execute(new SendUMessagesThread(this.cc, 200));
//                es.execute(new SendUMessagesThread(this.cc, 300));
//
//                // wait for join()s
//                es.shutdown();
//                while (!es.awaitTermination(24L, TimeUnit.HOURS)) {
//                    System.out.println("Not yet. Still waiting for termination");
//                }
//            } catch (IOException | InterruptedException e) {
//                // do nothing, error message is printed in SCThread
//            }
//        }
//    }
//
//    protected class SendUMessagesThread extends Thread {
//        UPSChannel cc;
//        int seqNum;
//        public SendUMessagesThread (UPSChannel cc, int seqNum) {
//            this.cc = cc;
//            this.seqNum = seqNum;
//        }
//
//        @Override
//        public void run() {
//            try {
//                System.out.println("==>Trying to send");
//
//                UpsAmazon.UTruckReady uTruckReady = UpsAmazon.UTruckReady.newBuilder().
//                        setSeqnum(this.seqNum).
//                        setTruckid(250).
//                        setWhid(25250).
//                        build();
//
//                UpsAmazon.UMessages msg = UpsAmazon.UMessages.newBuilder().addTruckReadies(uTruckReady).build();
//                this.cc.send(msg);
//
//                System.out.println("==>Sent");
//            } catch (MsgException e) {
//                System.out.println(e.getMsg());
//            }
//        }
//    }
//
//    protected class RecvUMessagesThread implements Runnable{
//        UPSChannel cc;
//
//        public RecvUMessagesThread (UPSChannel cc) {
//            this.cc = cc;
//        }
//
//        @Override
//        public void run() {
//            for (int i =0; i<3; i++) {
//                try {
//                    UpsAmazon.UMessages msg = this.cc.recv();
//                    System.out.println(msg.toString());
//                } catch (MsgException e) {
//                    System.out.println(e.getMsg());
//                }
//            }
//        }
//    }
//}
