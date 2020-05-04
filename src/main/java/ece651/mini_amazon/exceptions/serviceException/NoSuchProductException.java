package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class NoSuchProductException extends MsgException {
    public NoSuchProductException (String str) {
        super(str);
    }
}
