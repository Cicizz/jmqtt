package org.jmqtt.remoting.exception;

import java.io.Serializable;

public class RemotingTimeoutException extends RemotingException implements Serializable {

    private static final long serialVersionUID = -56656565470505110L;

    public RemotingTimeoutException(long timeoutMillis) {
        this(timeoutMillis,null);
    }

    public RemotingTimeoutException(long timeoutMillis, Throwable throwable) {
        super("Send timeout time: <" + timeoutMillis + ">", throwable);
    }
}
