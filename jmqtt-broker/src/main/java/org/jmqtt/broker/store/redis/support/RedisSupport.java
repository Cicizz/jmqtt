package org.jmqtt.broker.store.redis.support;

import org.jmqtt.broker.store.redis.RedisCallBack;

/**
 * redis 支持类，定义主要的redis操作
 */
public interface RedisSupport {
    /**
     * 封装基本的redis操作，对{@link RedisCallBack} 暴露了Jedis对象
     * @param redisCallBack
     * @param <T>
     * @return
     */
    <T> T operate(RedisCallBack redisCallBack);
}
