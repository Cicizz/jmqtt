package org.jmqtt.bus;

import org.jmqtt.bus.impl.*;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.subscription.CTrieSubscriptionMatcher;
import org.jmqtt.bus.subscription.SubscriptionMatcher;
import org.jmqtt.support.config.BrokerConfig;

public class BusController {

    private BrokerConfig brokerConfig;
    private Authenticator authenticator;
    private DeviceSessionManager deviceSessionManager;
    private DeviceMessageManager deviceMessageManager;
    private DeviceSubscriptionManager deviceSubscriptionManager;
    private SubscriptionMatcher subscriptionMatcher;
    private ClusterEventManager clusterEventManager;

    public BusController(BrokerConfig brokerConfig){
        this.brokerConfig = brokerConfig;
        this.authenticator = new DefaultAuthenticator();
        this.deviceMessageManager = new DeviceMessageManagerImpl();
        this.deviceSessionManager = new DeviceSessionManagerImpl();
        //this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        this.subscriptionMatcher = new CTrieSubscriptionMatcher();
        this.deviceSubscriptionManager = new DeviceSubscriptionManagerImpl(subscriptionMatcher);
        this.clusterEventManager = new ClusterEventManagerImpl(subscriptionMatcher);
    }


    public void start(){
        DBUtils.getInstance().start(brokerConfig);
        this.clusterEventManager.start();
    }

    public void shutdown(){
        this.clusterEventManager.shutdown();
        DBUtils.getInstance().shutdown();
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public DeviceSessionManager getDeviceSessionManager() {
        return deviceSessionManager;
    }

    public DeviceMessageManager getDeviceMessageManager() {
        return deviceMessageManager;
    }

    public DeviceSubscriptionManager getDeviceSubscriptionManager() {
        return deviceSubscriptionManager;
    }

    public ClusterEventManager getClusterEventManager() {
        return clusterEventManager;
    }

    public SubscriptionMatcher getSubscriptionMatcher() {
        return subscriptionMatcher;
    }
}
