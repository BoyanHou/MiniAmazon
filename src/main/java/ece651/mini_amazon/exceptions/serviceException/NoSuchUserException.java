package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class NoSuchUserException extends MsgException {
    public NoSuchUserException(String str) {
        super(str);
    }
}
