package org.jmqtt.store.redis;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.AbstractMqttStore;

public class RedisMqttStore extends AbstractMqttStore {
   private StoreConfig redisConfig;
   private RedisStoreManager redisStoreManager;

   public RedisMqttStore(StoreConfig redisConfig){ this.redisConfig = redisConfig; }

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
