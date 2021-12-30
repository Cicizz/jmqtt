package org.jmqtt.bus;

import org.jmqtt.bus.model.DeviceSubscription;

import java.util.Set;

public interface DeviceSubscriptionManager {

    void storeSubscription(DeviceSubscription deviceSubscription);

    Set<DeviceSubscription> getAllSubscription(String clientId);

    void updateSubscription(DeviceSubscription subscription);

    void deleteSubscription(String clientId,String topic);

    void deleteAllSubscription(String clientId);
}
