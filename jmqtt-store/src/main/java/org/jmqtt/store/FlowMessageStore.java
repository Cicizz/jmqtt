package org.jmqtt.store;


import org.jmqtt.common.model.Message;

import java.util.Collection;

/**
 * 存储与释放过程消息
 */
public interface FlowMessageStore {

    void clearClientFlowCache(String clientId);

    Message getRecMsg(String clientId, int msgId);

    boolean cacheRecMsg(String clientId,Message message);

    Message releaseRecMsg(String clientId,int msgId);

    boolean cacheSendMsg(String clientId,Message message);

    Collection<Message> getAllSendMsg(String clientId);

    boolean releaseSendMsg(String clientId,int msgId);

    boolean containSendMsg(String clientId,int msgId);

}
