package org.jmqtt.store;


import org.jmqtt.common.model.Subscription;

import java.util.Collection;

public interface SubscriptionStore {

    boolean storeSubscription(String clientId, Subscription subscription);

    Collection<Subscription> getSubscriptions(String clientId);

    boolean clearSubscription(String clientId);

    boolean removeSubscription(String clientId,String topic);

}
