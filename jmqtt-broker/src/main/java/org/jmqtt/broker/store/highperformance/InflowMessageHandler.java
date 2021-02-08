package org.jmqtt.broker.store.highperformance;

import org.jmqtt.broker.common.model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 入栈过程消息处理
 * 高性能配置下：利用内存进行缓存，不持久化
 */
public class InflowMessageHandler {

    private Map<String /* clientId */, Map<Integer /* msgId */,Message>> infowMsgMap = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    /**
     * 缓存qos2 publish报文消息-入栈消息
     * @return true:缓存成功   false:缓存失败
     */
    public boolean cacheInflowMsg(String clientId, Message message){
        Map<Integer,Message> msgCache = infowMsgMap.get(clientId);
        if (msgCache == null) {
            synchronized (lock) {
                msgCache = infowMsgMap.get(clientId);
                if (msgCache == null) {
                    msgCache = new ConcurrentHashMap<>();
                    infowMsgMap.put(clientId,msgCache);
                }
            }
        }
        msgCache.put(message.getMsgId(),message);
        return true;
    }

    /**
     * 获取并删除接收到的qos2消息-入栈消息
     */
    public Message releaseInflowMsg(String clientId,int msgId){
        Map<Integer,Message> msgCache = infowMsgMap.get(clientId);
        if (msgCache == null) {
            return null;
        }
        return msgCache.remove(msgId);
    }

    /**
     * 获取所有的入栈消息
     */
    public Collection<Message> getAllInflowMsg(String clientId){
        Map<Integer,Message> msgCache = infowMsgMap.get(clientId);
        if (msgCache == null || msgCache.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        Collection<Message> values = msgCache.values();
        Collection<Message> queue = new PriorityQueue<Message>(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (o1.getStoreTime() < o2.getStoreTime()) {
                    return -1;
                } else if (o1.getStoreTime() == o2.getStoreTime()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Message value : values) {
            queue.add(value);
        }
        return queue;
    }
}
