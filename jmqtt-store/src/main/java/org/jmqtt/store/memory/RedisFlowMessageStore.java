package org.jmqtt.store.memory;

import net.sf.json.JSONObject;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.FlowMessageStore;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class RedisFlowMessageStore implements FlowMessageStore {

//    private ConcurrentHashMap<String,Message> recCache = new ConcurrentHashMap<>();
//    private ConcurrentHashMap<String,Message> sendCache = new ConcurrentHashMap<>();
    private RedisStoreUtil recCache;
    private RedisStoreUtil sendCache;
    public  RedisFlowMessageStore(RedisConfig Config){
        recCache = new RedisStoreUtil(Config,"recCache:");
        sendCache = new RedisStoreUtil(Config,"sendCache:");
    }

    @Override
    public void clearClientFlowCache(String clientId) {
        recCache.delete(clientId);
        sendCache.delete(clientId);
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        return recCache.getMsg(clientId,msgId);
    }

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        return recCache.storeMsg(clientId,message);
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) { return recCache.releaseMsg(clientId,msgId); }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        return sendCache.storeMsg(clientId,message);
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        return sendCache.getAllMsg(clientId);
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
         sendCache.releaseMsg(clientId,msgId);
         return true;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return sendCache.containMsg(clientId,msgId);
    }

}
