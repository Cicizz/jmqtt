
package org.jmqtt.bus.event;

import org.jmqtt.bus.model.ClusterEvent;

/**
 * gateway listener
 * iot gateway 实现该接口，接收bus的事件进行客户端触达和处理
 */
public interface GatewayListener {

    void consume(ClusterEvent clusterEvent);
}
