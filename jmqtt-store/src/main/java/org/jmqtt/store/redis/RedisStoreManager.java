package org.jmqtt.store.redis;

import org.jmqtt.common.config.StoreConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.LinkedHashSet;
import java.util.Set;

public class RedisStoreManager {
    private StoreConfig redisConfig;
    private Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
    private JedisCluster cluster;
    private JedisPoolConfig poolConfig = new JedisPoolConfig();
    private volatile static RedisStoreManager INSTANCE ;

    private RedisStoreManager(){

    }

    public static RedisStoreManager getInstance(StoreConfig redisConfig){
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
        String[] clusterNodes = redisConfig.getNodes().split(";");
        for (String node : clusterNodes){
            nodes.add(new HostAndPort(node.split(":")[0],Integer.valueOf(node.split(":")[1])));
        }
        this.cluster = new JedisCluster(nodes,poolConfig);
    }

    public void shutDown(){
        try {
            cluster.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
