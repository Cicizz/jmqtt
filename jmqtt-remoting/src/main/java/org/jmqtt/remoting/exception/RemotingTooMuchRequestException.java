package org.jmqtt.remoting.exception;

import java.io.Serializable;

public class RemotingTooMuchRequestException  extends RemotingException implements Serializable {

    private static final long serialVersionUID = -865546545670505110L;

    public RemotingTooMuchRequestException(String message) {
        super(message);
    }

    public RemotingTooMuchRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
