package org.jmqtt.store.memory;

import org.jmqtt.common.model.Message;
import org.jmqtt.store.FlowMessageStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFlowMessageStore implements FlowMessageStore {

    private Map<String, ConcurrentHashMap<Integer,Message>> recCache = new ConcurrentHashMap<>();
    private Map<String, ConcurrentHashMap<Integer,Message>> sendCache = new ConcurrentHashMap<>();

    @Override
    public void clearClientFlowCache(String clientId) {
        this.recCache.remove(clientId);
        this.sendCache.remove(clientId);
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        return recCache.get(clientId).get(msgId);
    }


    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        if(!recCache.containsKey(clientId)){
            synchronized (recCache){
                if(!recCache.containsKey(clientId)){
                    recCache.put(clientId,new ConcurrentHashMap<Integer,Message>());
                }
            }
        }
        this.recCache.get(clientId).put(message.getMsgId(),message);
        return true;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        return this.recCache.get(clientId).remove(msgId);
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        if(!sendCache.containsKey(clientId)){
            synchronized (sendCache){
                if(!sendCache.containsKey(clientId)){
                    this.sendCache.put(clientId,new ConcurrentHashMap<>());
                }
            }
        }
        this.sendCache.get(clientId).put(message.getMsgId(),message);
        return true;
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        if(sendCache.containsKey(clientId)){
            return sendCache.get(clientId).values();

        }
        return new ArrayList<>();
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        this.sendCache.get(clientId).remove(msgId);
        return true;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return this.sendCache.get(clientId).containsKey(msgId);
    }
}