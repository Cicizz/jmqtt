package org.jmqtt.broker.processor;

import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.model.Message;
import org.jmqtt.common.model.MessageHeader;
import org.jmqtt.store.RetainMessageStore;

public abstract class AbstractMessageProcessor {

    private MessageDispatcher    messageDispatcher;
    private RetainMessageStore   retainMessageStore;

    public AbstractMessageProcessor(MessageDispatcher messageDispatcher, RetainMessageStore retainMessageStore) {
        this.messageDispatcher = messageDispatcher;
        this.retainMessageStore = retainMessageStore;
    }

    protected void processMessage(Message message) {
        this.messageDispatcher.appendMessage(message);
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if (retain) {
            int qos = (int) message.getHeader(MessageHeader.QOS);
            byte[] payload = message.getPayload();
            String topic = (String) message.getHeader(MessageHeader.TOPIC);
            //qos == 0 or payload is none,then clear previous retain message
            if (qos == 0 || payload == null || payload.length == 0) {
                this.retainMessageStore.removeRetainMessage(topic);
            } else {
                this.retainMessageStore.storeRetainMessage(topic, message);
            }
        }
        dispatcherMessage2Cluster(message);
    }

    private void dispatcherMessage2Cluster(Message message) {

    }

}
