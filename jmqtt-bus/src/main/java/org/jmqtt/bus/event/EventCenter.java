package org.jmqtt.bus.event;

import com.google.common.eventbus.EventBus;
import org.jmqtt.bus.model.ClusterEvent;

public class EventCenter {

    private final EventBus eventBus;

    public EventCenter(){
        this.eventBus = new EventBus();
    }

    /**
     * register gateway listener
     * @param listener
     */
    public void register(GatewayListener listener){
        this.eventBus.register(listener);
    }

    /**
     * send event to bus,and gateway will consume this event
     * @param clusterEvent
     */
    public void sendEvent(ClusterEvent clusterEvent){
        this.eventBus.post(clusterEvent);
    }
}
