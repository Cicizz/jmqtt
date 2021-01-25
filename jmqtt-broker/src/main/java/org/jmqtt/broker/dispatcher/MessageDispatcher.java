package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.common.model.Message;

/**
 * 消息分发器：
 */
public interface MessageDispatcher {

    void start();

    void shutdown();

    boolean appendMessage(Message message);

}
