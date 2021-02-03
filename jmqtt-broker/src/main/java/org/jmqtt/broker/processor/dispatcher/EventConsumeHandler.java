package org.jmqtt.broker.processor.dispatcher;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 集群事件处理
 */
public class EventConsumeHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.EVENT);

    private InnerMessageDispatcher innerMessageDispatcher;

    public EventConsumeHandler(BrokerController brokerController) {
        this.innerMessageDispatcher = brokerController.getInnerMessageDispatcher();
    }

    // 集群方式1: consume event from cluster
    public void consumeEvent(Event event){

    }

    // 集群方式2: poll event from cluster


    public void start(){

    }


    public void shutdown(){

    }


}
