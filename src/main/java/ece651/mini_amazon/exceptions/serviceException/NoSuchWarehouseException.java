package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class NoSuchWarehouseException extends MsgException {
    public NoSuchWarehouseException (String str) {
        super(str);
    }
}
