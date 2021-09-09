package org.jmqtt.broker.processor.dispatcher;

import java.util.List;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.processor.dispatcher.event.Event;

/**
 * send current node's event to other nodes 集群事件处理，两种方式,根据实际情况，选一个实现即可： 1. 发布订阅： a. 第一阶段：发送消息 jmqtt
 * A -> mq系统转发 b. 第二阶段：消息订阅分发给本节点的设备 jmqtt B,C,D —> jmqtt B的连接设备 2. jmqtt服务主动拉取: a. 第一阶段:消息发送 jmqtt
 * A —> 消息存储服务： b. 第二阶段:主动拉取 jmqtt B,C,D broker 定时批量拉取 —> 从消息存储服务拉取 —> 拉取后推送给 B,C,D上连接的设备
 */
public interface ClusterEventHandler {

    void start(BrokerConfig brokerConfig);

    void shutdown();

    /**
     * send event to cluster
     */
    boolean sendEvent(Event event);

    /**
     * consume cluster event: cluster push event to node 集群方式1：集群主动push消息过来,在实现类中消费事件后调用
     * EventConsumeHandler.consumeEvent()方法来让Jmqtt处理事件
     */
    void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler);

    /**
     * consume cluster event: poll from cluster 集群方式2：集群无法主动push，由jmqtt主动拉消息
     */
    List<Event> pollEvent(int maxPollNum);
}
