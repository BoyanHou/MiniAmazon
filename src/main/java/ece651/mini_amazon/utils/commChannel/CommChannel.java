package ece651.mini_amazon.utils.commChannel;

import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import com.google.protobuf.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class CommChannel {
    protected SocketChannel channel;
    protected String ip;
    protected int port;

    public Socket getSocket() {return this.channel.socket();}

    public CommChannel(SocketChannel channel,
                       String ip,
                       int port) throws IOException{
        this.channel = channel;
        this.ip = ip;
        this.port = port;
        this.channel.configureBlocking(true);
    }

    public synchronized void send (GeneratedMessageLite<?, ?> msg) throws DisconnectionException {
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            msg.writeDelimitedTo(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            this.channel.write(ByteBuffer.wrap(byteArray));

        } catch (IOException e) {  // currently treat all IOException as socket connection error
            String errMsg = "IOException happened during attempt to send to " + this.ip + ":" + this.port;
            throw new DisconnectionException(errMsg);
        }
    }
}
