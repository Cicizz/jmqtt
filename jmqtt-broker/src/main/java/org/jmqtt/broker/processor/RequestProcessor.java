package org.jmqtt.broker.processor;

import org.jmqtt.common.bean.Message;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     */
    void processRequest(Message message);
}
