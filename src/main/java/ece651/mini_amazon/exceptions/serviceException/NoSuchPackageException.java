package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class NoSuchPackageException extends MsgException {
    public NoSuchPackageException (String str) {
        super(str);
    }
}
