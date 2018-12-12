package org.jmqtt.store;

import org.jmqtt.common.bean.Subscription;

import java.util.Collection;

public interface SubscriptionStore {

    boolean storeSubscription(String clientId, Subscription subscription);

    Collection<Subscription> getSubscriptions(String clientId);

    boolean clearSubscription(String clientId);

}
