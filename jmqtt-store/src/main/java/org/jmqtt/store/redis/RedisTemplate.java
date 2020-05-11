
package org.jmqtt.store.redis;

import org.apache.commons.lang3.StringUtils;
import org.jmqtt.common.config.ClusterConfig;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTemplate {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private ClusterConfig clusterConfig;
    private JedisPool jedisPool;

    public static final String PROJECT = "JMQTT";

    public RedisTemplate(ClusterConfig clusterConfig){
        this.clusterConfig = clusterConfig;
    }

    public void init(){
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMinIdle(clusterConfig.getMinIdle());
            jedisPoolConfig.setMaxTotal(clusterConfig.getMaxTotal());
            jedisPoolConfig.setTestOnBorrow(clusterConfig.isTestOnBorrow());
            jedisPoolConfig.setMaxTotal(jedisPoolConfig.getMaxIdle());
            jedisPoolConfig.setMaxWaitMillis(jedisPoolConfig.getMaxWaitMillis());
            if (StringUtils.isEmpty(clusterConfig.getRedisPassword())) {
                jedisPool = new JedisPool(jedisPoolConfig,clusterConfig.getRedisHost(),clusterConfig.getRedisPort());
            } else {
                jedisPool = new JedisPool(jedisPoolConfig,clusterConfig.getRedisHost(),clusterConfig.getRedisPort(),10000,clusterConfig.getRedisPassword());
            }
        } catch (Exception ex) {
            log.error("[Redis handle error],ex:{}",ex);
        }
    }

    public Object operate(RedisCallBack redisCallBack){
        log.debug("[Cluster] redis operate begin");
        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return redisCallBack.operate(jedis);
        }catch (Exception ex) {
            log.error("[Cluster] redis operate error,ex:{}",ex);
        } finally {
            log.debug("[Cluster] redis operate cost:{}",(System.currentTimeMillis() - startTime));
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public void close(){
        log.info("[Cluster] redis close");
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

}