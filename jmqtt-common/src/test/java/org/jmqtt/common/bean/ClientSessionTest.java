package org.jmqtt.common.bean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientSessionTest {

    private ClientSession clientSession;

    @Before
    public void init(){
        this.clientSession = new ClientSession();
    }
    @Test
    public void generateMessageId() {
        for(int i = 0; i < Integer.MAX_VALUE; i++){
            int messageId = clientSession.generateMessageId();
            Assert.assertTrue(messageId > 0);
            Assert.assertTrue(messageId < 0xFFFF);
        }
    }
}