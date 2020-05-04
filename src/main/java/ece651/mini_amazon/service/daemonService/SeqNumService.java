package ece651.mini_amazon.service.daemonService;

import org.springframework.stereotype.Service;

@Service
public class SeqNumService {
    protected int allocatableSeqNum;

    public synchronized int getSeqNum() {
        int result = this.allocatableSeqNum;
        this.allocatableSeqNum++;
        return this.allocatableSeqNum;
    }

    public void init (int initialSeqNum) {
        System.out.println("==>Initializing SeqNum...");
        this.allocatableSeqNum = initialSeqNum;
        System.out.println("==>Finished Initializing SeqNum: " + this.allocatableSeqNum);
    }
}
