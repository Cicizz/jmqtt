package org.jmqtt.broker.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.redis.support.RedisKeySupport;
import org.jmqtt.broker.store.redis.support.RedisReplyUtils;
import org.jmqtt.broker.store.redis.support.RedisSupport;
import org.jmqtt.broker.store.redis.support.RedisUtils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisMessageStore implements MessageStore {

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
    public boolean storeWillMessage(String clientId, Message message) {
        return RedisReplyUtils.isOk(redisSupport.operate(jedis -> jedis.set(RedisKeySupport.WILL+clientId, JSONObject.toJSONString(message))));
    }

    @Override
    public boolean clearWillMessage(String clientId) {
        redisSupport.operate(jedis -> jedis.del(RedisKeySupport.WILL+clientId));
        return true;
    }

    @Override
    public Message getWillMessage(String clientId) {
        return JSONObject.parseObject(redisSupport.operate(jedis -> jedis.get(RedisKeySupport.WILL+clientId)),Message.class);
    }

    @Override
    public boolean storeRetainMessage(String topic, Message message) {
        redisSupport.operate(jedis -> jedis.hset(RedisKeySupport.RETAIN,String.valueOf(topic),JSONObject.toJSONString(message)));
        return true;
    }

    @Override
    public boolean clearRetaionMessage(String topic) {
        redisSupport.operate(jedis -> jedis.hdel(RedisKeySupport.RETAIN,topic));
        return true;
    }

    @Override
    public Collection<Message> getAllRetainMsg() {
        Map<String,String> retainMessages = redisSupport.operate(jedis -> jedis.hgetAll(RedisKeySupport.RETAIN));
        return retainMessages.values().stream().map(val->JSONObject.parseObject(val,Message.class)).collect(Collectors.toList());
    }
}
