package org.jmqtt.group;

import org.jmqtt.common.bean.Message;

public interface ClusterMessageResolver {

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
    void send(Message message);

    /**
     * 从集群中接收消息
     */
    Message receive();
}
