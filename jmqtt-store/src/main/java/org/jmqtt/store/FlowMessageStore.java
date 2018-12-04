package org.jmqtt.store;

import org.jmqtt.common.bean.Message;

/**
 * 存储与释放过程消息
 */
public interface FlowMessageStore {

    /**
     * 初始化每个客户端的过程消息存储介质（qos1,qos2）
     */
    void initClientFlowCache(String clientId);

    void clearClientFlowCache(String clientId);

    Message getRecMsg(String clientId,int msgId);

    boolean cacheRecMsg(String clientId,Message message);

    Message releaseRecMsg(String clientId,int msgId);

    boolean cacheSendMsg(String clientId,Message message);

    boolean releaseSendMsg(String clientId,int msgId);

    boolean containSendMsg(String clientId,int msgId);

}
