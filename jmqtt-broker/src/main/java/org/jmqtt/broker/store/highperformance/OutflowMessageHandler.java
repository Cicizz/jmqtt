package org.jmqtt.broker.store.highperformance;

import org.jmqtt.broker.common.model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 出栈过程消息处理
 * 高性能配置下：利用内存进行缓存，不持久化
 */
public class OutflowMessageHandler {

    private Map<String /* clientId */, Map<Integer /* msgId */, Message>> outflowMsgCache = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    public boolean cacheOutflowMsg(String clientId, Message message) {
        Map<Integer,Message> msgCache = outflowMsgCache.get(clientId);
        if (msgCache == null) {
            synchronized (lock) {
                msgCache = outflowMsgCache.get(clientId);
                if (msgCache == null) {
                    msgCache = new ConcurrentHashMap<>();
                    outflowMsgCache.put(clientId,msgCache);
                }
            }
        }
        msgCache.put(message.getMsgId(),message);
        return true;
    }

    public boolean containOutflowMsg(String clientId, int msgId) {
        Map<Integer,Message> msgCache = outflowMsgCache.get(clientId);
        if (msgCache == null) {
            return false;
        }
        if (msgCache.containsKey(msgId)) {
            return true;
        }
        return false;
    }

    public Collection<Message> getAllOutflowMsg(String clientId) {
        Map<Integer,Message> msgCache = outflowMsgCache.get(clientId);
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

    public Message releaseOutflowMsg(String clientId, int msgId) {
        Map<Integer,Message> msgCache = outflowMsgCache.get(clientId);
        if (msgCache == null) {
            return null;
        }
        return msgCache.remove(msgId);
    }

}
