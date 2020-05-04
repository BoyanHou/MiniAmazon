package ece651.mini_amazon.service.daemonService;

import com.google.protobuf.GeneratedMessageLite;
import ece651.mini_amazon.exceptions.commException.DisconnectionException;
import ece651.mini_amazon.utils.commChannel.CommChannel;
import ece651.mini_amazon.utils.tools.Logger;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageProcessingService {

    protected HashSet<Integer> processedSeqnums;
    protected ExecutorService threadPool;

    public MessageProcessingService  () {
        this.processedSeqnums = new HashSet<>();
        this.threadPool = Executors.newCachedThreadPool();
        String msg = "==>Created: MessageProcessingService";
        Logger.logSys(msg);
    }


    protected synchronized void recordSeqNum(int seqnum) {
        if (!this.processedSeqnums.contains(seqnum)) {
            this.processedSeqnums.add(seqnum);
            Logger.logSys("==>U"+ seqnum + ": recorded new sequence number from UPS!");
        }
    }

    protected class AckSendingTask extends Thread {
        CommChannel channel;
        GeneratedMessageLite<?, ?> acks;

        public AckSendingTask (CommChannel channel, GeneratedMessageLite<?, ?> acks) {
            this.channel = channel;
            this.acks = acks;
        }

        public void run () {
            try {
                this.channel.send(this.acks);
            } catch (DisconnectionException e) {
                Logger.logErr("Failed to send ACKs because connection breaks: " + e.getMsg());
            }
        }
    }

}
