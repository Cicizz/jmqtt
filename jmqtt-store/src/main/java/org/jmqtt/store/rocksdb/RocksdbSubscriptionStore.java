package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.SubscriptionStore;
import org.jmqtt.store.rocksdb.utils.RocksHash;
import org.jmqtt.store.rocksdb.utils.RocksMap;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class RocksdbSubscriptionStore implements SubscriptionStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksHash rocksHash;

    public RocksdbSubscriptionStore(RocksdbDao rocksdbDao){
        this.rocksHash = rocksdbDao.getRocksHash();
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        try{
            this.rocksHash.put(RocksdbStorePrefix.SUBSCRIPTION + clientId,subscription.getTopic(),SerializeHelper.serialize(subscription));
            return true;
        }catch(Exception ex){
            log.warn("Store subscription failure,clientId={},Subscription={}",clientId,subscription);
        }
        return false;
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        Collection<byte[]> values = this.rocksHash.values(clientId);
        Collection<Subscription> subscriptions = new ArrayList<>(values.size());
        for(byte[] value : values){
            subscriptions.add(SerializeHelper.deserialize(value,Subscription.class));
        }
        return subscriptions;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        try{
            this.rocksHash.clear(RocksdbStorePrefix.SUBSCRIPTION + clientId);
            return true;
        }catch (Exception ex){
            log.warn("Clear subscription failure,clientId={}",clientId);
            return false;
        }
    }

    @Override
    public boolean removeSubscription(String clientId, String topic) {
        try{
            this.rocksHash.remove(RocksdbStorePrefix.SUBSCRIPTION+clientId,topic);
            return true;
        }catch (Exception ex){
            log.warn("Remove subscription failure,clientId={}",clientId);
            return false;
        }
    }
}
