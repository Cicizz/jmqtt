package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;

import java.util.Collection;

public interface RedisDao {
    void delete(String clientId);
    Message getMsg(String clientId,Integer msgId);
    boolean storeMsg(String clientId,Message message);
    Message releaseMsg(String clientId,int msgId);
    Collection<Message> getAllMsg(String clientId);
    boolean containMsg(String clientId,int msgId);
}
