package org.jmqtt.broker.store.redis;

import redis.clients.jedis.Jedis;

public interface RedisCallBack<T> {

    T operate(Jedis jedis);
}
