package org.jmqtt.bus;

import org.jmqtt.bus.event.GatewayListener;
import org.jmqtt.bus.model.ClusterEvent;

/**
 * cluster event send and listen
 */
public interface ClusterEventManager {

    void sendEvent(ClusterEvent clusterEvent);

    void registerEventListener(GatewayListener listener);

    void start();

    void shutdown();
}
