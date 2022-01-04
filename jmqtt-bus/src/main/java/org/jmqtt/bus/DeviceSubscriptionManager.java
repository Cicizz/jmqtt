package org.jmqtt.bus;

import org.jmqtt.bus.model.DeviceSubscription;

import java.util.Set;

public interface DeviceSubscriptionManager {

    /**
     * 1. Add subscription to the sub tree
     * 2. Persistence subscription
     * @param deviceSubscription
     * @return true:sub success  false: sub fail
     */
    boolean subscribe(DeviceSubscription deviceSubscription);

    /**
     * 1. Remove subscription from the sub tree
     * 2. Delete subscription from db
     */
    boolean unSubscribe(String clientId,String topic);

    boolean isMatch(String pubTopic,String subTopic);

    boolean onlySubscribe2Tree(DeviceSubscription deviceSubscription);

    boolean onlyUnUnSubscribeFromTree(String clientId,String topic);

    Set<DeviceSubscription> getAllSubscription(String clientId);

    void deleteAllSubscription(String clientId);
}
