package org.jmqtt.broker.subscribe;

import org.jmqtt.common.bean.Subscription;

import java.util.Set;

public interface SubscriptionMatcher {

    boolean subscribe(String topic, Subscription subscription);

    boolean unSubscribe(String topic,String clientId);

    Set<Subscription> match(String topic);

}
