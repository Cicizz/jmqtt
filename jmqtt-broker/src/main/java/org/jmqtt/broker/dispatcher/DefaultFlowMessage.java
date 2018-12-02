package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.bean.Message;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFlowMessage implements FlowMessage {

    private Map<String, ConcurrentHashMap<Integer,Message>> recCache = new ConcurrentHashMap<>();
    private Map<String, ConcurrentHashMap<Integer,Message>> sendCache = new ConcurrentHashMap<>();

    @Override
    public void initClientFlowCache(String clientId){
        Map<Integer,Message> recMsgCache = recCache.get(clientId);
        if(Objects.isNull(recMsgCache)){
            recCache.put(clientId,new ConcurrentHashMap<Integer,Message>());
        }
        Map<Integer,Message> sendMsgCache = sendCache.get(clientId);
        if(Objects.isNull(recMsgCache)){
            sendCache.put(clientId,new ConcurrentHashMap<Integer,Message>());
        }
    }

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
        this.recCache.get(clientId).put(message.getMsgId(),message);
        return true;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        return this.recCache.get(clientId).remove(msgId);
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        this.sendCache.get(clientId).put(message.getMsgId(),message);
        return true;
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        this.sendCache.get(clientId).remove(msgId);
        return true;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return this.sendCache.get(clientId).contains(msgId);
    }
}
