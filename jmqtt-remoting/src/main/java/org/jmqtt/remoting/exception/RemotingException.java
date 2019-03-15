package org.jmqtt.remoting.exception;

import java.io.Serializable;

public class RemotingException extends Exception implements Serializable {
    private static final long serialVersionUID = -46545454570505110L;

    public RemotingException(String message){
        super(message);
    }

    public RemotingException(String message,Throwable throwable){
        super(message,throwable);
    }

}
