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
        if(Objects.nonNull(recMsgCache)){
            recCache.put(clientId,new ConcurrentHashMap<Integer,Message>());
        }
        Map<Integer,Message> sendMsgCache = sendCache.get(clientId);
        if(Objects.nonNull(recMsgCache)){
            sendCache.put(clientId,new ConcurrentHashMap<Integer,Message>());
        }
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        return recCache.get(clientId).get(msgId);
    }

    ;

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        return this.recCache.get(clientId).put(message.getMsgId(),message) != null;
    }

    @Override
    public boolean releaseRecMsg(String clientId, int msgId) {
        return this.recCache.get(clientId).remove(msgId) != null;
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        return false;
    }
}
