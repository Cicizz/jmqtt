package org.jmqtt.store.memory;

import org.jmqtt.common.model.Subscription;
import org.jmqtt.store.SubscriptionStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSubscriptionStore implements SubscriptionStore {

    private Map<String, ConcurrentHashMap<String,Subscription>> subscriptionCache = new ConcurrentHashMap<>();


    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        if(!subscriptionCache.containsKey(clientId)){
            synchronized (subscriptionCache){
                if(!subscriptionCache.containsKey(clientId)){
                    subscriptionCache.put(clientId,new ConcurrentHashMap<>());
                }
            }
        }
        return subscriptionCache.get(clientId).put(subscription.getTopic(),subscription) != null;
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        Map<String,Subscription> subscriptionMap = subscriptionCache.get(clientId);
        if(Objects.nonNull(subscriptionMap)){
            return subscriptionMap.values();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean clearSubscription(String clientId) {
        this.subscriptionCache.remove(clientId);
        return true;
    }

    @Override
    public boolean removeSubscription(String clientId, String topic) {
        if(this.subscriptionCache.containsKey(clientId)){
            this.subscriptionCache.remove(topic);
        }
        return true;
    }
}
