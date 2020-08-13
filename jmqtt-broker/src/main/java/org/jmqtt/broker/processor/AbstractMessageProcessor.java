package org.jmqtt.broker.processor;

import org.jmqtt.broker.cluster.ClusterMessageTransfer;
import org.jmqtt.broker.cluster.command.CommandCode;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.broker.cluster.redis.RedisClusterMessageTransfer;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.model.Message;
import org.jmqtt.common.model.MessageHeader;
import org.jmqtt.store.RetainMessageStore;

public abstract class AbstractMessageProcessor {

    private MessageDispatcher messageDispatcher;
    private RetainMessageStore retainMessageStore;
    private ClusterMessageTransfer clusterMessageTransfer;

    public AbstractMessageProcessor(MessageDispatcher messageDispatcher, RetainMessageStore retainMessageStore, ClusterMessageTransfer clusterMessageTransfer) {
        this.messageDispatcher = messageDispatcher;
        this.retainMessageStore = retainMessageStore;
        this.clusterMessageTransfer = clusterMessageTransfer;
    }

    protected void processMessage(Message message) {
        /**
         * TODO Bug说明:生产者生产一条消息,消费者会收到两条消息
         *  暂时解决办法：新增一个集群类型的判断，用于解决redis集群时，这里不在向队列中放入数据,
         *  在各broker收到订阅的Topic消息时再存入数据，发送到客户端。
         */
        if (!(clusterMessageTransfer instanceof RedisClusterMessageTransfer)) {
            this.messageDispatcher.appendMessage(message);
        }
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if (retain) {
            int qos = (int) message.getHeader(MessageHeader.QOS);
            byte[] payload = message.getPayload();
            String topic = (String) message.getHeader(MessageHeader.TOPIC);
            //qos == 0 or payload is none,then clear previous retain message
            if (qos == 0 || payload == null || payload.length == 0) {
                this.retainMessageStore.removeRetainMessage(topic);
            } else {
                this.retainMessageStore.storeRetainMessage(topic, message);
            }
        }
        dispatcherMessage2Cluster(message);
    }

    private void dispatcherMessage2Cluster(Message message) {
        CommandReqOrResp commandReqOrResp = new CommandReqOrResp(CommandCode.MESSAGE_CLUSTER_TRANSFER, message);
        if (clusterMessageTransfer != null) {
            clusterMessageTransfer.sendMessage(commandReqOrResp);
        }
    }

}
