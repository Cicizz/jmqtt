package org.jmqtt.store.redis;

import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.AbstractMqttStore;

public class RedisMqttStore extends AbstractMqttStore {
   private RedisConfig redisConfig;
   private RedisStoreManager redisStoreManager;

   public void RedisMqttStore(RedisConfig redisConfig){ this.redisConfig = redisConfig; }

    @Override
    public void init() throws Exception {
        this.redisStoreManager = RedisStoreManager.getInstance(redisConfig);
        this.flowMessageStore = new RedisFlowMessageStore(redisConfig);
        this.willMessageStore = new RedisWillMessageStore(redisConfig);
        this.retainMessageStore = new RedisRetainMessageStore(redisConfig);
        this.offlineMessageStore = new RedisOfflineMessageStore(redisConfig);
        this.subscriptionStore = new RedisSubscriptionStore(redisConfig);
        this.sessionStore = new RedisSessionStore(redisConfig);
    }

    @Override
    public void close() {
        redisStoreManager.shutDown();
    }
}
