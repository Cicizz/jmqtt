
package org.jmqtt.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.RetainMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RedisRetainMessageStore implements RetainMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String RETAIN = RedisTemplate.PROJECT + "_RETAIN";

    public RedisRetainMessageStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Collection<Message> getAllRetainMessage() {
        Object msgObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hgetAll(RETAIN);
            }
        });
        Collection<Message> messages = new ArrayList<>();
        if (msgObj != null) {
            Map<String, String> map = (Map<String, String>) msgObj;
            map.values().forEach(item -> {
                ((ArrayList<Message>) messages).add(JSONObject.parseObject(item, Message.class));
            });
        }
        return messages;
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hset(RETAIN, topic, JSONObject.toJSONString(message));
            }
        });
    }

    @Override
    public void removeRetainMessage(String topic) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hdel(RETAIN, topic);
            }
        });
    }
}