package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class StockNotEnoughException extends MsgException {
    public StockNotEnoughException (String str) {
        super(str);
    }
}
