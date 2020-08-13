package org.jmqtt.store.redis;

import redis.clients.jedis.Jedis;

public interface RedisCallBack {

    Object operate(Jedis jedis);
}