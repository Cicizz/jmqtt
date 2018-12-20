package org.jmqtt.store.redis;

import org.jmqtt.common.config.RedisConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.LinkedHashSet;
import java.util.Set;

public class RedisStoreManager {
    private RedisConfig redisConfig;
    private Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
    private JedisCluster cluster;
    private JedisPoolConfig poolConfig = new JedisPoolConfig();
    private volatile static RedisStoreManager INSTANCE ;

    private RedisStoreManager(){

    }

    public static RedisStoreManager getInstance(RedisConfig redisConfig){
        if (null == INSTANCE){
            synchronized (RedisStoreManager .class){
                if (null == INSTANCE){
                    INSTANCE = new RedisStoreManager();
                    INSTANCE.redisConfig = redisConfig;
                }
            }
        }
        return INSTANCE;
    }

    public JedisCluster getCluster(){return cluster;}

    public void initialization(){
        poolConfig.setMaxTotal(redisConfig.getMaxActive());
        poolConfig.setMaxIdle(redisConfig.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisConfig.getTimeout());
        for (Integer id=0;id<4;id++){
            nodes.add(new HostAndPort(redisConfig.getHost(id),redisConfig.getPort(id)));
        }
        cluster = new JedisCluster(nodes,poolConfig);
    }

    public void shutDown(){
        try {
            cluster.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
