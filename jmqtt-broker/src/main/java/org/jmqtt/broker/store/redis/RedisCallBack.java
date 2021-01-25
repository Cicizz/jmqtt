package org.jmqtt.broker.store.redis;

import redis.clients.jedis.Jedis;

public interface RedisCallBack {

    Object operate(Jedis jedis);
}
