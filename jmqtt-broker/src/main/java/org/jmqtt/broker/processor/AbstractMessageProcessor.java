package org.jmqtt.broker.processor;

import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;

public abstract class AbstractMessageProcessor {

    protected MessageDispatcher messageDispatcher;

    public AbstractMessageProcessor(MessageDispatcher messageDispatcher){
        this.messageDispatcher = messageDispatcher;
    }

    protected void  processMessage(Message message){
        this.messageDispatcher.appendMessage(message);
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if(retain){
            //TODO  处理retain消息

        }
    }

}
