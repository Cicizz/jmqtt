package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.remoting.netty.MessageDispatcher;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.memory.DefaultFlowMessageStore;
import org.junit.Before;
import org.junit.Test;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    @Before
    public void init(){
        SubscriptionMatcher subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        FlowMessageStore flowMessageStore = new DefaultFlowMessageStore();
        dispatcher = new DefaultDispatcherMessage(10,subscriptionMatcher, flowMessageStore);
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