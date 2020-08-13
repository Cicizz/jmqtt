package org.jmqtt.remoting.exception;

import java.io.Serializable;

public class RemotingConnectException extends RemotingException implements Serializable {

    private static final long serialVersionUID = -412312312370505110L;

    public RemotingConnectException(String addr) {
        this(addr,null);
    }

    public RemotingConnectException(String addr, Throwable throwable) {
        super("connect to <" + addr + "> failed", throwable);
    }
}
