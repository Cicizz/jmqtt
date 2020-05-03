package org.jmqtt.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisWillMessageStore implements WillMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String WILL = RedisTemplate.PROJECT + "_WILL_";

    public RedisWillMessageStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Message getWillMessage(String clientId) {
        Object msgStr = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.get(WILL + clientId);
            }
        });
        if (msgStr == null) {
            log.warn("[Redis] getWillMessage error,message is null,clientId:{}", clientId);
            return null;
        }
        return JSONObject.parseObject(msgStr.toString(), Message.class);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        Object existsObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.exists(WILL + clientId);
            }
        });
        return BooleanUtils.toBoolean(existsObj.toString());
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.set(WILL + clientId,JSONObject.toJSONString(message));
            }
        });
    }

    @Override
    public Message removeWillMessage(String clientId) {
        Object msgObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                String msgStr = jedis.get(WILL + clientId);
                jedis.del(WILL + clientId);
                return msgStr;
            }
        });
        if (msgObj == null) {
            log.warn("[Redis] removeWillMessage error,message is null,clientId:{}",clientId);
            return null;
        };
        return JSONObject.parseObject(msgObj.toString(),Message.class);
    }
}