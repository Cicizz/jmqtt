package org.jmqtt.broker.exception;

public class BrokerException extends Exception{

    private String message;

    public BrokerException(String message){
        super(message);
    }

}
