package org.jmqtt.group;

import org.jmqtt.common.bean.Message;

public interface ClusterResolver {

    /**
     * 启动集群消息处理器
     */
    void start();

    /**
     * 关闭集群消息处理器
     */
    void shutdown();

    /**
     * 发送消息到集群
     */
    void send(ClusterRemotingCommand message);

    /**
     * 从集群中接收到消息
     */
    ClusterRemotingCommand receive();
}
