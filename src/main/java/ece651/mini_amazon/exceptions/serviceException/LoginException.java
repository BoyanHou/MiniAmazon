package ece651.mini_amazon.exceptions.serviceException;

import ece651.mini_amazon.exceptions.MsgException;

public class LoginException extends MsgException {
    public LoginException (String str) {
        super(str);
    }
}
