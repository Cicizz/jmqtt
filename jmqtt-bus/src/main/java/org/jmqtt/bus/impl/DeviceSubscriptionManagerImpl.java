package org.jmqtt.bus.impl;

import org.jmqtt.bus.DeviceSubscriptionManager;
import org.jmqtt.bus.model.DeviceSubscription;

import java.util.Set;


public class DeviceSubscriptionManagerImpl implements DeviceSubscriptionManager {
    @Override
    public void storeSubscription(DeviceSubscription deviceSubscription) {

    }

    @Override
    public Set<DeviceSubscription> getAllSubscription(String clientId) {
        return null;
    }

    @Override
    public void updateSubscription(DeviceSubscription subscription) {

    }

    @Override
    public void deleteSubscription(String clientId, String topic) {

    }

    @Override
    public void deleteAllSubscription(String clientId) {

    }
}
