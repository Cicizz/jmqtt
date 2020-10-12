package org.jmqtt.remoting.exception;

import java.io.Serializable;

public class RemotingSendRequestException extends RemotingException implements Serializable {

    public RemotingSendRequestException(String message) {
        this(message,null);
    }

    public RemotingSendRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
