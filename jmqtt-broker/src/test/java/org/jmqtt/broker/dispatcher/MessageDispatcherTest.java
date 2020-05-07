package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.OfflineMessageStore;
import org.jmqtt.store.memory.DefaultFlowMessageStore;
import org.jmqtt.store.memory.DefaultOfflineMessageStore;
import org.junit.Before;
import org.junit.Test;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    @Before
    public void init(){
        SubscriptionMatcher subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        FlowMessageStore flowMessageStore = new DefaultFlowMessageStore();
        OfflineMessageStore offlineMessageStore = new DefaultOfflineMessageStore();
        dispatcher = new DefaultDispatcherMessage(10,subscriptionMatcher, flowMessageStore,offlineMessageStore);
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