package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.bean.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    @Before
    public void init(){
        dispatcher = new DefaultDispatcherMessage(10);
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