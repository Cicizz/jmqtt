package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.model.Message;

public interface MessageDispatcher {

    void start();

    void shutdown();

    boolean appendMessage(Message message);

}
