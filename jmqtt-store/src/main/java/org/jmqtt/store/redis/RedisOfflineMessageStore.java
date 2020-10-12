package org.jmqtt.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.OfflineMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RedisOfflineMessageStore implements OfflineMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String OFFLINE = RedisTemplate.PROJECT + "_OFFLINE_";

    public RedisOfflineMessageStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void clearOfflineMsgCache(String clientId) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.del(OFFLINE + clientId);
            }
        });
    }

    @Override
    public boolean containOfflineMsg(String clientId) {
        Object existsObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.exists(OFFLINE + clientId);
            }
        });
        return BooleanUtils.toBoolean(existsObj.toString());
    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hset(OFFLINE + clientId, String.valueOf(message.getMsgId()), JSONObject.toJSONString(message));
            }
        });
        return true;
    }

    @Override
    public Collection<Message> getAllOfflineMessage(String clientId) {
        Object msgObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hgetAll(OFFLINE + clientId);
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
}