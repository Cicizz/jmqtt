package org.jmqtt.broker.store.mem;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MemSessionStore extends AbstractMemStore implements SessionStore {
    private static final Object OBJECT = new Object();
    private static final Logger log = JmqttLogger.storeLog;
    /**
     * 离线消息
     */
    private final Map<String,/*clientId*/ BlockingQueue<Message>> offlineTable = new ConcurrentHashMap<>();
    /**
     * 离线消息警告阈值，超过当前数量的客户端会出警告
     */
    private int msgMaxNum = 1000;
    /**
     * 过程消息
     */
    private final Map<String,/*clientId*/ ConcurrentHashMap<Integer,/*msgId*/Message>> recCache = new ConcurrentHashMap<>();
    private final Map<String,/*clientId*/ ConcurrentHashMap<Integer,/*msgId*/Message>> sendCache = new ConcurrentHashMap<>();
    private final Map<String,/*clientId*/ ConcurrentHashMap<Integer,/*msgId*/Object>> secTwoCache = new ConcurrentHashMap<>();

    /**
     * session
     */
    private Map<String, SessionState> sessionTable = new ConcurrentHashMap<>();
    /**
     * sub
     */
    private final Map<String, ConcurrentHashMap<String, Subscription>> subscriptionCache = new ConcurrentHashMap<>();

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public SessionState getSession(String clientId) {
        SessionState s = sessionTable.get(clientId);
        if(s==null){
            // 从未连接过
            return new SessionState(SessionState.StateEnum.NULL);
        }
        return s;
    }

    @Override
    public boolean storeSession(String clientId, SessionState sessionState) {
        sessionTable.put(clientId,sessionState);
        return true;
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        ConcurrentHashMap<String, Subscription> v = subscriptionCache.get(clientId);
        if(v == null){
            synchronized (subscriptionCache){
                v = subscriptionCache.get(clientId);
                if(v == null){
                    v = new ConcurrentHashMap<>();
                    subscriptionCache.put(clientId,v);
                }
            }
        }
        v.put(subscription.getTopic(), subscription);
        return true;
    }

    @Override
    public boolean delSubscription(String clientId, String topic) {
        ConcurrentHashMap<String, Subscription> v = subscriptionCache.get(clientId);
        if(v != null){
            v.remove(topic);
        }else{
            LogUtil.warn(log,"[MemStore] -> Client:{} does not have a subscription for this topic:{}",clientId,topic);
        }
        return true;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        subscriptionCache.remove(clientId);
        return true;
    }

    @Override
    public Set<Subscription> getSubscriptions(String clientId) {
        ConcurrentHashMap<String, Subscription> v = subscriptionCache.get(clientId);
        if(v == null){
            return new HashSet<Subscription>();
        }
        Collection<Subscription> sub  = v.values();
        return new HashSet<>(sub);
    }

    @Override
    public boolean cacheInflowMsg(String clientId, Message message) {
        ConcurrentHashMap<Integer, Message> v =  recCache.get(clientId);
        if(v == null){
            synchronized (recCache){
                v = recCache.get(clientId);
                if(v==null){
                    v = new ConcurrentHashMap<>();
                    recCache.put(clientId,v);
                }
            }
        }
        v.put(message.getMsgId(),message);
        return true;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        ConcurrentHashMap<Integer, Message> v =  recCache.get(clientId);
        if(v == null){
            LogUtil.warn(log,"[MemStore] -> The inflow message:{} does not exist",msgId);
            return null;
        }
        return v.remove(msgId);
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        ConcurrentHashMap<Integer, Message> v =  recCache.get(clientId);
        if(v == null || v.isEmpty()){
            return new ArrayList<Message>();
        }
        return v.values();
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        ConcurrentHashMap<Integer, Message> v =  sendCache.get(clientId);
        if(v == null){
            synchronized (sendCache){
                v = sendCache.get(clientId);
                if(v == null){
                    v = new ConcurrentHashMap<>();
                    sendCache.put(clientId,v);
                }
            }
        }
        v.put(message.getMsgId(),message);
        return true;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        ConcurrentHashMap<Integer, Message> v =  sendCache.get(clientId);
        if(v == null || v.isEmpty()){
            return new ArrayList<Message>();
        }
        return v.values();
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        ConcurrentHashMap<Integer, Message> v =  sendCache.get(clientId);
        if(v == null){
            LogUtil.warn(log,"[MemStore] -> The out of the stack message:{} does not exist",msgId);
            return null;
        }
        return v.remove(msgId);
    }

    @Override
    public boolean cacheOutflowSecMsgId(String clientId, int msgId) {
        ConcurrentHashMap<Integer,Object> v =  secTwoCache.get(clientId);
        if(v == null){
            synchronized (secTwoCache){
                v = secTwoCache.get(clientId);
                if(v == null){
                    v = new ConcurrentHashMap<>();
                    secTwoCache.put(clientId,v);
                }
            }
        }
        v.put(msgId,OBJECT);
        return true;
    }

    @Override
    public boolean releaseOutflowSecMsgId(String clientId, int msgId) {
        ConcurrentHashMap<Integer,Object> v =  secTwoCache.get(clientId);
        if(v == null){
            LogUtil.warn(log,"[MemStore] -> The out flow QOS2 phase 2, client:{} outflow cache does not exist",clientId);
            return false;
        }
        Object os =  v.remove(msgId);
        if(os == null){
            LogUtil.warn(log,"[MemStore] -> The out flow QOS2 phase 2, msg:{} for client:{} does not exist",clientId,msgId);
        }
        return os != null;
    }

    @Override
    public List<Integer> getAllOutflowSecMsgId(String clientId) {
        ConcurrentHashMap<Integer,Object> v =  secTwoCache.get(clientId);
        if(v == null){
            return new ArrayList<Integer>();
        }
        ArrayList<Integer> ret = new ArrayList<>(v.size());
        v.forEach((k,v2)->{
            ret.add(k);
        });
        return ret;
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        BlockingQueue<Message> off = offlineTable.get(clientId);
        if(off == null){
            synchronized (offlineTable){
                off = offlineTable.get(clientId);
                if(off == null){
                    off = new LinkedBlockingQueue<>();
                    offlineTable.put(clientId,off);
                }
            }
        }
        off.add(message);
//        if(off.size() > msgMaxNum){ //影响性能
//            LogUtil.warn(log,"[MemStore] -> Client {} has more than {} offline messages",clientId,msgMaxNum);
//        }
        return true;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        BlockingQueue<Message> off = offlineTable.get(clientId);
        if(off == null){
            return new ArrayList<Message>();
        }
        return off;
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        offlineTable.remove(clientId);
        return true;
    }
}
