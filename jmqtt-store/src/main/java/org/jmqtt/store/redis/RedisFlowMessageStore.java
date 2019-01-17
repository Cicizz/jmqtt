package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.FlowMessageStore;

import java.util.Collection;

public class RedisFlowMessageStore implements FlowMessageStore {

    private RedisStoreUtil recCache;
    private RedisStoreUtil sendCache;
    public  RedisFlowMessageStore(StoreConfig Config){
        recCache = new RedisStoreUtil(Config,"recCache");
        sendCache = new RedisStoreUtil(Config,"sendCache");
    }

    @Override
    public void clearClientFlowCache(String clientId) {
        recCache.delete(clientId);
        sendCache.delete(clientId);
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) { return recCache.hgetMsg(clientId,msgId); }

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        return recCache.hstoreMsg(clientId,String.valueOf(message.getMsgId()),message);
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) { return recCache.hreleaseMsg(clientId,msgId); }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        return sendCache.hstoreMsg(clientId,String.valueOf(message.getMsgId()),message);
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        return sendCache.hgetAllMsg(clientId,Message.class);
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
         sendCache.hreleaseMsg(clientId,msgId);
         return true;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return sendCache.hcontainMsg(clientId,msgId);
    }

}
