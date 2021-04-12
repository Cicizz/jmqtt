package org.jmqtt.broker.subscribe;

import java.util.Set;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.model.Subscription;
import org.slf4j.Logger;


public class SubscriptionSupportGroupTreeMatcher implements SubscriptionMatcher {

    private static final Logger log = JmqttLogger.messageTraceLog;

    private GroupSubscriptionAndMessageListener groupSubscriptionAndMessageListener;
    private SubscriptionMatcher subscriptionMatcher;


    public SubscriptionSupportGroupTreeMatcher(
        GroupSubscriptionAndMessageListener groupSubscriptionAndMessageListener) {
        this.groupSubscriptionAndMessageListener = groupSubscriptionAndMessageListener;
        this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        if (SubscriptionMatcher.isGroupTopic(subscription.getTopic())) {
            groupSubscriptionAndMessageListener.subscribe(subscription);
            return true;
        } else {
            return subscriptionMatcher.subscribe(subscription);
        }
    }

    @Override
    public boolean unSubscribe(String topic, String clientId) {
        if (SubscriptionMatcher.isGroupTopic(topic)) {
            groupSubscriptionAndMessageListener.unSubscribe(topic, clientId);
            return true;
        } else {
            return subscriptionMatcher.unSubscribe(topic, clientId);
        }
    }


    @Override
    public Set<Subscription> match(String topic, String clientId) {
        return subscriptionMatcher.match(topic, clientId);
    }

    @Override
    public Set<Subscription> matchGroup(String topic, String clientId) {
        return subscriptionMatcher.matchGroup(topic, clientId);
    }


    @Override
    public boolean isMatch(String pubTopic, String subTopic) {
        return subscriptionMatcher.isMatch(pubTopic, subTopic);
    }
}
