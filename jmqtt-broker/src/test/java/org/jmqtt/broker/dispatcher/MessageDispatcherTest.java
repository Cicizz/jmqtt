package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    @Before
    public void init(){
        SubscriptionMatcher subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        FlowMessage flowMessage = new DefaultFlowMessage();
        dispatcher = new DefaultDispatcherMessage(10,subscriptionMatcher,flowMessage);
        dispatcher.start();
    }

    @Test
    public void start() {
        dispatcher.start();
    }

    @Test
    public void shutdown() {
    }

    @Test
    public void appendMessage() {
        Message message = new Message();
        boolean rs = dispatcher.appendMessage(message);
        assert rs == true;
    }
}