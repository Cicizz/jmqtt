package org.jmqtt.broker.processor;

import org.jmqtt.remoting.netty.MessageDispatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.store.RetainMessageStore;

public abstract class AbstractMessageProcessor {

    protected MessageDispatcher messageDispatcher;

    private RetainMessageStore retainMessageStore;

    public AbstractMessageProcessor(MessageDispatcher messageDispatcher,RetainMessageStore retainMessageStore){
        this.messageDispatcher = messageDispatcher;
        this.retainMessageStore = retainMessageStore;
    }

    protected void  processMessage(Message message){
        this.messageDispatcher.appendMessage(message);
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if(retain){
            int qos = (int) message.getHeader(MessageHeader.QOS);
            byte[] payload = (byte[])message.getPayload();
            String topic = (String) message.getHeader(MessageHeader.TOPIC);
            //qos == 0 or payload is none,then clear previous retain message
            if(qos == 0 || payload.length == 0){
                this.retainMessageStore.removeRetainMessage(topic);
            }else{
                this.retainMessageStore.storeRetainMessage(topic,message);
            }
        }
    }

}
