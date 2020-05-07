
package org.jmqtt.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Subscription;
import org.jmqtt.store.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RedisSubscriptionStore implements SubscriptionStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String SUBSCRIPTION = RedisTemplate.PROJECT + "_SUBSCRIPTION_";

    public RedisSubscriptionStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hset(SUBSCRIPTION + clientId, subscription.getTopic(), JSONObject.toJSONString(subscription));
            }

            ;
        });
        return true;
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        Object subObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hgetAll(SUBSCRIPTION + clientId);
            }
        });
        Collection<Subscription> subscriptions = new ArrayList<>();
        if (subObj != null) {
            Map<String, String> map = (Map<String, String>) subObj;
            map.values().forEach(item -> {
                ((ArrayList<Subscription>) subscriptions).add(JSONObject.parseObject(item, Subscription.class));
            });
        }
        return subscriptions;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.del(SUBSCRIPTION + clientId);
            }
        });
        return true;
    }

    @Override
    public boolean removeSubscription(String clientId, String topic) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hdel(SUBSCRIPTION + clientId, topic);
            }
        });
        return true;
    }
}