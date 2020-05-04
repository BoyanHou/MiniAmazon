package ece651.mini_amazon.exceptions.commException;

public class ConnectionException extends ReconnectionException {
    public ConnectionException (String str) {
        super(str);
    }

}
