package org.jmqtt.store.redis;

import org.jmqtt.common.config.ClusterConfig;
import org.jmqtt.store.AbstractMqttStore;

public class RedisMqttStore extends AbstractMqttStore {

    private ClusterConfig clusterConfig;
    private RedisTemplate redisTemplate;

    public RedisMqttStore(ClusterConfig clusterConfig) {
        this.clusterConfig = new ClusterConfig();
    }

    @Override
    public void init() throws Exception {
        this.redisTemplate = new RedisTemplate(clusterConfig);
        this.redisTemplate.init();

        this.flowMessageStore = new RedisFlowMessageStore(redisTemplate);
        this.willMessageStore = new RedisWillMessageStore(redisTemplate);
        this.retainMessageStore = new RedisRetainMessageStore(redisTemplate);
        this.offlineMessageStore = new RedisOfflineMessageStore(redisTemplate);
        this.subscriptionStore = new RedisSubscriptionStore(redisTemplate);
        this.sessionStore = new RedisSessionStore(redisTemplate);
    }

    @Override
    public void shutdown() {
        redisTemplate.close();
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }
}