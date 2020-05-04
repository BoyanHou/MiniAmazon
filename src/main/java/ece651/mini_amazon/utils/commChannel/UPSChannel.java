package ece651.mini_amazon.utils.commChannel;

import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import protobuf_generated.UpsAmazon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class UPSChannel extends CommChannel{

//    ByteArrayInputStream is;

    public UPSChannel(SocketChannel channel, String ip, int port) throws IOException {
        super(channel, ip, port);
//        byte[] bytes = new byte[0];
//        this.is = new ByteArrayInputStream(bytes);
    }


    // public receive method
    public UpsAmazon.UMessages recv () throws DisconnectionException {
        String prompt = " during attempt to receive from UPS " + this.ip + ":" + this.port;

        UpsAmazon.UMessages message;
//        try {
//            // register channel as read
//            this.channel.register(selector, SelectionKey.OP_READ);
//        } catch (ClosedChannelException e) {
//            String errMsg = "ClosedChannelException" + prompt;
//            throw new DisconnectionException(errMsg);
//        }
        try {
//            if (this.is.available() == 0) {  // if stored inputstream is exhausted: acquire new data from channel
//                System.out.println("read");
//                ByteBuffer buf = ByteBuffer.allocate(2048);
//
//                this.channel.register(this.selector, SelectionKey.OP_READ);
//                this.selector.select(); // blocks until there are any bytes to be read
//
//                int len = this.channel.read(buf);
//                if (len == -1) {
//                    throw new DisconnectionException("ERROR: Received len is -1. connection to " + this.ip + ":" + this.port);
//                }
//                System.out.println(len);
//                this.is = new ByteArrayInputStream(buf.array());
//            }

//            selector.select(); // block until there are any bytes to be read

            message = UpsAmazon.UMessages.parseDelimitedFrom(this.channel.socket().getInputStream());
        } catch (IOException e) {  // currently treat all IOException as socket connection error
            String errMsg = "IOException" + prompt;
            throw new DisconnectionException(errMsg);
        }
        return message;
    }
}
