
package org.jmqtt.broker.cluster;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.*;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.cluster.command.CommandCode;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.config.ClusterConfig;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultClusterMessageTransfer extends ClusterMessageTransfer {

    private static final String HAZELCAST_JMQTT = "jmqtt";

    private HazelcastInstance hazelcastInstance = null;

    private static final String T_JMQTT_HAZELCAST = "T_JMQTT_HAZELCAST";

    private ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryImpl("HAZELCAST_CLUSTER_CONSUME_THREAD"));

    private ClusterConfig clusterConfig;

    public DefaultClusterMessageTransfer(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    public DefaultClusterMessageTransfer(MessageDispatcher messageDispatcher, ClusterConfig clusterConfig) {
        super(messageDispatcher);
        this.clusterConfig = clusterConfig;
    }

    @Override
    public void startup() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                subscribe();
            }
        });
    }

    private void subscribe() {
        HazelcastInstance getHazelcastInstance = Hazelcast.getHazelcastInstanceByName(HAZELCAST_JMQTT);
        if (getHazelcastInstance != null) {
            log.warn("Hazelcast already initialized");
            this.hazelcastInstance = getHazelcastInstance;
        }
        Config config = new Config();
        config.setInstanceName(HAZELCAST_JMQTT);
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        //关闭组播协议组建集群
        joinConfig.getMulticastConfig().setEnabled(false);
        //开启TCP协议组建集群
        joinConfig.getTcpIpConfig().setEnabled(true);
        //设置连接超时时间
        config.getNetworkConfig().getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(30);
        if (null != clusterConfig && StringUtils.isNotBlank(clusterConfig.getClusterMember())) {
            String clusterMember = clusterConfig.getClusterMember();
            String[] members = clusterMember.split(",");
            for (String member : members) {
                String clusterMemberAddress = member + ":5701";
                joinConfig.getTcpIpConfig().addMember(clusterMemberAddress);
            }
        } else {
            throw new NullPointerException("ClusterConfig non-existent");
        }
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        ITopic<String> topic = this.hazelcastInstance.getTopic(T_JMQTT_HAZELCAST);
        topic.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                CommandReqOrResp request = new CommandReqOrResp(CommandCode.MESSAGE_CLUSTER_TRANSFER, JSONObject.parseObject(message.getMessageObject(),
                        org.jmqtt.common.model.Message.class));
                consumeClusterMessage(request);
            }
        });
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public CommandReqOrResp sendMessage(CommandReqOrResp commandReqOrResp) {
        ITopic<String> topic = this.hazelcastInstance.getTopic(T_JMQTT_HAZELCAST);
        topic.publish(JSONObject.toJSONString(commandReqOrResp.getBody()));
        CommandReqOrResp response = new CommandReqOrResp(commandReqOrResp.getCommandCode());
        return response;
    }
}