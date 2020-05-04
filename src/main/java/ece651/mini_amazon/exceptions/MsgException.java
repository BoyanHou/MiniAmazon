package ece651.mini_amazon.exceptions;

public class MsgException extends Exception{
    String msg;
    public MsgException(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return this.msg;
    }
}
