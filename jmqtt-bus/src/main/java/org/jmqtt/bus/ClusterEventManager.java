package org.jmqtt.bus;

import org.jmqtt.bus.model.ClusterEvent;

/**
 * cluster event send and listen
 */
public interface ClusterEventManager {

    void sendEvent(ClusterEvent clusterEvent);

}
