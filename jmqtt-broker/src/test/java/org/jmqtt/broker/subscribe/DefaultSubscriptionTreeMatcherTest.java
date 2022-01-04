package org.jmqtt.broker.subscribe;

import org.jmqtt.bus.subscription.DefaultSubscriptionTreeMatcher;
import org.jmqtt.bus.subscription.SubscriptionMatcher;
import org.jmqtt.bus.subscription.model.Subscription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DefaultSubscriptionTreeMatcherTest {

    private SubscriptionMatcher subscriptionMatcher;

    @Before
    public void init() {
        this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
    }

    @Test
    public void subscribe() {
        String topic = "MQTT_TEST/2/+/E/#";
        String clientId = "clientid_test";
        Subscription subscription = new Subscription(clientId, topic, new HashMap<>());
        Assert.assertTrue(this.subscriptionMatcher.subscribe(subscription));
    }

    @Test
    public void match() {
        String[] topics = new String[3];
        String[] clientIds = new String[12];
        topics[0] = "T/+/+/T_TEST_1";
        topics[1] = "$share/g1/T/+/2/+";
        topics[2] = "$share/g1/T/1/2/+";
        for (int i = 0; i < 12; i++) {
            clientIds[i] = "C_clientId_" + i;
            String topic = topics[i % 3];
            Subscription subscription = new Subscription(clientIds[i], topic, new HashMap<>());
            subscriptionMatcher.subscribe(subscription);
        }

        Set<Subscription> subscriptions = new HashSet<>();
        String subTopic = "T/1/2/T_TEST_1";
        Set<Subscription> tempClientIds = subscriptionMatcher.match(subTopic);
        subscriptions.addAll(tempClientIds);
        System.out.println(
            "topic:" + subTopic + " has " + subscriptions.size() + "  subs:" + subscriptions);

    }
}
