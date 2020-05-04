package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class PurchaseFailure extends MsgException {
    public PurchaseFailure (String str) {
        super(str);
    }
}
