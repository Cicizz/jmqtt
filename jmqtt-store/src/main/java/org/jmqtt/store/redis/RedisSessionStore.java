
package org.jmqtt.store.redis;

import org.apache.commons.lang3.BooleanUtils;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String SESSION = RedisTemplate.PROJECT + "_SESSION_";

    public RedisSessionStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean containSession(String clientId) {
        Object existsObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.exists(SESSION + clientId);
            }
        });
        return BooleanUtils.toBoolean(existsObj.toString());
    }

    @Override
    public Object setSession(String clientId, Object obj) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.set(SESSION + clientId, String.valueOf(obj));
            }
        });
        return obj;
    }

    @Override
    public Object getLastSession(String clientId) {
        return redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.get(SESSION + clientId);
            }
        });
    }

    @Override
    public boolean clearSession(String clientId) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.del(SESSION + clientId);
            }
        });
        return true;
    }
}