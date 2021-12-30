package org.jmqtt.bus;

import org.jmqtt.bus.impl.ClusterEventManagerImpl;
import org.jmqtt.bus.impl.DeviceMessageManagerImpl;
import org.jmqtt.bus.impl.DeviceSessionManagerImpl;
import org.jmqtt.bus.impl.DeviceSubscriptionManagerImpl;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.support.config.BrokerConfig;

public class BusController {

    private BrokerConfig brokerConfig;
    private Authenticator authenticator;
    private DeviceSessionManager deviceSessionManager;
    private DeviceMessageManager deviceMessageManager;
    private DeviceSubscriptionManager deviceSubscriptionManager;
    private ClusterEventManager clusterEventManager;

    public BusController(BrokerConfig brokerConfig){
        this.brokerConfig = brokerConfig;
        this.deviceMessageManager = new DeviceMessageManagerImpl();
        this.deviceSessionManager = new DeviceSessionManagerImpl();
        this.deviceSubscriptionManager = new DeviceSubscriptionManagerImpl();
        this.clusterEventManager = new ClusterEventManagerImpl();
    }


    public void start(){
        DBUtils.getInstance().start(brokerConfig);
    }

    public void shutdown(){
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
}
