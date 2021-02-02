package org.jmqtt.broker.processor.protocol;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.processor.dispatcher.MessageDispatcher;
import org.jmqtt.broker.processor.HighPerformanceMessageHandler;
import org.jmqtt.broker.store.MessageStore;

/**
 * 通用消息分发处理器
 *
 *  集群消息分发模式（第二阶段逻辑：{@link MessageDispatcher}）：
 *      1. 发布订阅：
 *          a. 第一阶段：发送消息 jmqtt A -> mq系统转发
 *          b. 第二阶段：消息订阅分发给本节点的设备 jmqtt B,C,D —> jmqtt B的连接设备
 *      2. jmqtt服务主动拉取:
 *          a. 第一阶段:消息发送 jmqtt A —> 消息存储服务：
 *          b. 第二阶段:主动拉取 jmqtt B,C,D broker 定时批量拉取 —> 从消息存储服务拉取 —> 拉取后推送给 B,C,D上连接的设备
 * TODO mqtt5实现
 */
public abstract class AbstractMessageProcessor extends HighPerformanceMessageHandler {

    private MessageStore messageStore;

    public AbstractMessageProcessor(BrokerController brokerController) {
        super(brokerController);
        this.messageStore = brokerController.getMessageStore();
    }

    protected void processMessage(Message message) {

        // 1. retain消息逻辑
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if (retain) {
            int qos = (int) message.getHeader(MessageHeader.QOS);
            byte[] payload = message.getPayload();
            String topic = (String) message.getHeader(MessageHeader.TOPIC);
            //qos == 0 or payload is none,then clear previous retain message
            if (qos == 0 || payload == null || payload.length == 0) {
                this.messageStore.clearRetaionMessage(topic);
            } else {
                this.messageStore.storeRetainMessage(topic, message);
            }
        }
        // 2. 向集群中分发消息：第一阶段
        sendMessage2Cluster(message);
    }

    /**
     * 向集群分发消息:第一阶段
     * TODO 插件化实现，二次开发可实现向自己的转发集群中发送消息
     */
    private void sendMessage2Cluster(Message message) {

    }

}
