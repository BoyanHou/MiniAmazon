package ece651.mini_amazon.utils.commChannel;

import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import protobuf_generated.UpsAmazon;
import protobuf_generated.WorldAmazon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class WorldChannel extends CommChannel{
//    Selector selector;
    public WorldChannel(SocketChannel channel, String ip, int port) throws IOException {
        super(channel, ip, port);
//        this.selector = Selector.open();
    }

    // public receive method
    public WorldAmazon.AResponses recv () throws DisconnectionException {
        String prompt = " happened during attempt to receive from World " + this.ip + ":" + this.port;
        WorldAmazon.AResponses message;
//        try {
//            // register channel as read
//            this.channel.register(selector, SelectionKey.OP_READ);
//        } catch (ClosedChannelException e) {
//            String errMsg = "ClosedChannelException" + prompt;
//            throw new DisconnectionException(errMsg);
//        }

        try {
            // block until there are any bytes to be read
//            selector.select();

            message = WorldAmazon.AResponses.parseDelimitedFrom(this.channel.socket().getInputStream());
        } catch (IOException e) {  // currently treat all IOException as socket connection error
            String errMsg = "IOException" + prompt;
            throw new DisconnectionException(errMsg);
        }
        return message;
    }


}
