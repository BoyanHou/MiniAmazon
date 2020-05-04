package ece651.mini_amazon.utils.stub;

import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import ece651.mini_amazon.utils.commChannel.CommChannel;
import protobuf_generated.WorldAmazon;
import protobuf_generated.WorldUps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;

public class UPSWorldChannelStub extends CommChannel {
    public UPSWorldChannelStub(SocketChannel channel, String ip, int port) throws IOException {
        super(channel, ip, port);
    }

    // public receive method
    public WorldUps.UResponses recv () throws DisconnectionException {
        String prompt = " happened during attempt to receive from World " + this.ip + ":" + this.port;
        WorldUps.UResponses message;
        InputStream is = new ByteArrayInputStream(new byte[10]);
        try {
            this.channel.socket().getInputStream();
        } catch (IOException e) {
            throw new DisconnectionException("");
        }
        try {
            message = WorldUps.UResponses.parseDelimitedFrom(is);
        } catch (IOException e) {
            throw new DisconnectionException("");
        }
        return message;
    }
}
