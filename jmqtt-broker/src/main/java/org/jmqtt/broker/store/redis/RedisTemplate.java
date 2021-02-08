
package org.jmqtt.broker.store.redis;

import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTemplate {

    private static final Logger log = JmqttLogger.storeLog;

    private BrokerConfig brokerConfig;
    private JedisPool    jedisPool;

    public static final String PROJECT = "JMQTT";

    public RedisTemplate(BrokerConfig brokerConfig){
        this.brokerConfig = brokerConfig;
    }

    public void init(){
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMinIdle(brokerConfig.getMinIdle());
            jedisPoolConfig.setMaxTotal(brokerConfig.getMaxTotal());
            jedisPoolConfig.setTestOnBorrow(brokerConfig.isTestOnBorrow());
            jedisPoolConfig.setMaxTotal(jedisPoolConfig.getMaxIdle());
            jedisPoolConfig.setMaxWaitMillis(jedisPoolConfig.getMaxWaitMillis());
            if (StringUtils.isEmpty(brokerConfig.getRedisPassword())) {
                jedisPool = new JedisPool(jedisPoolConfig,brokerConfig.getRedisHost(),brokerConfig.getRedisPort());
            } else {
                jedisPool = new JedisPool(jedisPoolConfig,brokerConfig.getRedisHost(),brokerConfig.getRedisPort(),10000,brokerConfig.getRedisPassword());
            }
        } catch (Exception ex) {
            LogUtil.error(log,"[Redis handle error],ex:{}",ex);
        }
    }

    public <T> T operate(RedisCallBack redisCallBack){
        LogUtil.debug(log,"[Cluster] redis operate begin");
        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return redisCallBack.operate(jedis);
        }catch (Exception ex) {
            LogUtil.error(log,"[Cluster] redis operate error,ex:{}",ex);
        } finally {
            LogUtil.debug(log,"[Cluster] redis operate cost:{}",(System.currentTimeMillis() - startTime));
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public void close(){
        LogUtil.info(log,"[Cluster] redis close");
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

}
