package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.SubscriptionStore;

import java.util.Collection;

public class RedisSubscriptionStore implements SubscriptionStore {
    private RedisStoreUtil subscritionCache;

    public RedisSubscriptionStore(StoreConfig redisConfig){
        this.subscritionCache = new RedisStoreUtil(redisConfig,"subscritionCache:");
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        subscritionCache.hstoreMsg(clientId,subscription.getTopic(),subscription);
        return true;
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        return subscritionCache.hgetAllMsg(clientId,Subscription.class);
    }

    @Override
    public boolean clearSubscription(String clientId) {
        subscritionCache.delete(clientId);
        return true;
    }

    @Override
    public boolean removeSubscription(String clientId, String topic) {
        return false;
    }
}
