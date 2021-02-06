package org.jmqtt.broker.store.redis.support;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.store.redis.RedisCallBack;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * redis 访问模板方法类
 */
public class RedisUtils {
    private static final RedisUtils redisUtils = new RedisUtils();
    private volatile RedisSupportImpl redisSupport;

    private AtomicBoolean start = new AtomicBoolean(false);

    private RedisUtils(){}

    public static RedisUtils getInstance(){
        return redisUtils;
    }

    public RedisSupport createSupport(BrokerConfig brokerConfig) {
        if (start.compareAndSet(false,true)) {
            this.redisSupport = new RedisSupportImpl(brokerConfig);
            this.redisSupport.init();
        }
        return redisSupport;
    }

    public <T> T operate(RedisCallBack redisCallBack) {
        return this.redisSupport.operate(redisCallBack);
    }

    public void close() {
        RedisSupportImpl redisSupport = this.redisSupport;
        if(start.compareAndSet(true,false)&&redisSupport!=null){
            redisSupport.close();
        }
    }
}
