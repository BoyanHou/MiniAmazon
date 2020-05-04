package ece651.mini_amazon.utils.concurrentTools;

public class Notifier {
    protected boolean satisfied;

    public void Notifier() {
        this.satisfied = false;
    }

    public void setSatisfied() {
        this.satisfied = true;
    }

    public boolean isSatisfied() {
        return this.satisfied;
    }

}
