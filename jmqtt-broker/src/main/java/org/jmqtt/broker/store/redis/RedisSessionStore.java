package org.jmqtt.broker.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.redis.support.RedisKeySupport;
import org.jmqtt.broker.store.redis.support.RedisReplyUtils;
import org.jmqtt.broker.store.redis.support.RedisSupport;
import org.jmqtt.broker.store.redis.support.RedisUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisSessionStore implements SessionStore {

    private RedisSupport redisSupport;

    @Override
    public void start(BrokerConfig brokerConfig) {
        this.redisSupport = RedisUtils.getInstance().createSupport(brokerConfig);
    }

    @Override
    public void shutdown() {
        RedisUtils.getInstance().close();
    }

    @Override
    public SessionState getSession(String clientId) {
        SessionState sessionState = redisSupport.operate(jedis -> JSONObject.parseObject(jedis.get(RedisKeySupport.SESSION + clientId), SessionState.class));
        if (sessionState == null) {
            return new SessionState(SessionState.StateEnum.NULL);
        }
        return sessionState;
    }

    @Override
    public boolean storeSession(String clientId, SessionState sessionState) {
        return RedisReplyUtils.isOk(redisSupport.operate(jedis -> jedis.set(RedisKeySupport.SESSION + clientId, JSONObject.toJSONString(sessionState))));
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.SUBSCRIPTION + clientId, subscription.getTopic(), JSONObject.toJSONString(subscription)));
        return true;
    }

    @Override
    public boolean delSubscription(String clientId, String topic) {
        redisSupport.operate(jedis -> jedis.hdel(RedisKeySupport.SUBSCRIPTION, topic));
        return true;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        redisSupport.operate(jedis -> jedis.del(RedisKeySupport.SUBSCRIPTION + clientId));
        return true;
    }

    @Override
    public Set<Subscription> getSubscriptions(String clientId) {
        Map<String, String> subscriptions = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.SUBSCRIPTION + clientId));
        return subscriptions.values().stream().map(val -> JSONObject.parseObject(val, Subscription.class)).collect(Collectors.toSet());
    }

    @Override
    public boolean cacheInflowMsg(String clientId, Message message) {
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.REC_FLOW_MESSAGE + clientId, String.valueOf(message.getMsgId()), JSONObject.toJSONString(message)));
        return true;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        Message message = JSONObject.parseObject(redisSupport.operate(jedis -> jedis.hget(RedisKeySupport.REC_FLOW_MESSAGE, String.valueOf(msgId))), Message.class);
        redisSupport.operate(jedis -> jedis.del(RedisKeySupport.REC_FLOW_MESSAGE, String.valueOf(msgId)));
        return message;
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        Map<String, String> inflowMessages = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.REC_FLOW_MESSAGE + clientId));
        return inflowMessages.values().stream().map(val -> JSONObject.parseObject(val, Message.class)).collect(Collectors.toList());
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.SEND_FLOW_MESSAGE + clientId, String.valueOf(message.getMsgId()), JSONObject.toJSONString(message)));
        return true;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        Map<String, String> outflowMessages = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.SEND_FLOW_MESSAGE + clientId));
        return outflowMessages.values().stream().map(val -> JSONObject.parseObject(val, Message.class)).collect(Collectors.toList());
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        return JSONObject.parseObject(redisSupport.operate(jedis -> jedis.hdel(RedisKeySupport.SEND_FLOW_MESSAGE + clientId, String.valueOf(msgId))), Message.class);
    }

    @Override
    public boolean cacheOutflowSecMsgId(String clientId, int msgId) {
        String value = String.valueOf(msgId);
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.SEND_FLOW_SEC_MESSAGE + clientId, value, value));
        return true;
    }

    @Override
    public boolean releaseOutflowSecMsgId(String clientId, int msgId) {
        redisSupport.operate(jedis -> jedis.hdel(RedisKeySupport.SEND_FLOW_SEC_MESSAGE + clientId, String.valueOf(msgId)));
        return true;
    }

    @Override
    public List<Integer> getAllOutflowSecMsgId(String clientId) {
        Map<String, String> secMsgIds = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.SEND_FLOW_SEC_MESSAGE + clientId));
        return secMsgIds.values().stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.OFFLINE + clientId, String.valueOf(message.getMsgId()), JSONObject.toJSONString(message)));
        return true;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        Map<String, String> offlineMessages = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.OFFLINE + clientId));
        return offlineMessages.values().stream().map(val -> JSONObject.parseObject(val, Message.class)).collect(Collectors.toList());
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        redisSupport.operate(jedis -> jedis.del(RedisKeySupport.OFFLINE + clientId));
        return true;
    }
}
