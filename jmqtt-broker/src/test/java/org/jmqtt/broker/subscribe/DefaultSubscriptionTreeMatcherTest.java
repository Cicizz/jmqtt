package org.jmqtt.broker.subscribe;

import org.jmqtt.common.model.Subscription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class DefaultSubscriptionTreeMatcherTest {

    private SubscriptionMatcher subscriptionMatcher;

    @Before
    public void init(){
        this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
    }

    @Test
    public void subscribe() {
        String topic = "MQTT_TEST/2/+/E/#";
        String clientId = "clientid_test";
        Subscription subscription = new Subscription(clientId,topic,1);
        Assert.assertTrue(this.subscriptionMatcher.subscribe(subscription));
    }

    @Test
    public void match() {
        String[] topics = new String[4];
        String[] clientIds = new String[10];
        for(int i = 0; i < 4; i++){
            topics[i] = "T_TEST_" + i;
        }
        for(int i = 0; i < 10; i++){
            clientIds[i] = "C_clientId_" + i;
            String topic = topics[i%4];
            Subscription subscription = new Subscription(clientIds[i],topic,1);
            subscriptionMatcher.subscribe(subscription);
        }
        Set<Subscription> subscriptions = new HashSet<>();
        for(int i = 0; i < 4; i++){
            Set<Subscription> tempCientIds = subscriptionMatcher.match("T_TEST_"+i);
            subscriptions.addAll(tempCientIds);
        }
    }
}