package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class RocksHash extends AbstractRocksHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);


    private Lock lock = new ReentrantLock();

    private Map<String/* key */, AtomicLong /* key的大小，计数器 */> metaHash = new ConcurrentHashMap<>();

    private RocksDB rocksDB;

    public RocksHash(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public byte[] get(String key,String field){
        try {
            return rocksDB.get((key+separator+field).getBytes());
        } catch (RocksDBException e) {
            log.warn("RockDB get Hash error,cause={}",e);
        }
        return null;
    }

    public long size(String key){
        try {
            if(!metaHash.containsKey(key)){
                synchronized (this){
                    if(!metaHash.containsKey(key)){
                        loadMetaHash(key);
                        return metaHash.get(key).get();
                    }
                }
            }else{
                return metaHash.get(key).get();
            }
        } catch (Exception e) {
            log.warn("RockDB get Hash error,cause={}",e);
        }
        return 0L;
    }

    public boolean contains(String key,String field){
        try {
            return rocksDB.get((key+separator+field).getBytes()) != null;
        } catch (RocksDBException e) {
            log.warn("RockDB get Hash error,cause={}",e);
        }
        return true;
    }

    public void put(String key,String field,byte[] value){
        try {
            if(!metaHash.containsKey(key)){
                synchronized (this){
                    if(!metaHash.containsKey(key)){
                        loadMetaHash(key);
                        long size = metaHash.get(key).incrementAndGet();
                        WriteBatch writeBatch = new WriteBatch();
                        writeBatch.put(getMetaKey(key),getMetaValue(size));
                        writeBatch.put(getDataKey(key,field),value);
                        this.rocksDB.write(new WriteOptions(),writeBatch);
                        return;
                    }
                }
            }
            long size = metaHash.get(key).incrementAndGet();
            WriteBatch writeBatch = new WriteBatch();
            writeBatch.put(getMetaKey(key),getMetaValue(size));
            writeBatch.put(getDataKey(key,field),value);
            this.rocksDB.write(new WriteOptions(),writeBatch);
        } catch (Exception e) {
            log.warn("RockDB store Hash error,cause={}",e);
        }
    }

    private void loadMetaHash(String key){
        try {
            byte[] isKeyExists = rocksDB.get(getMetaKey(key));
            AtomicLong size = null;
            if(isKeyExists == null){
                size = new AtomicLong(0);
            }else{
                size = new AtomicLong(SerializeHelper.deserialize(isKeyExists,Long.class));
            }
            this.metaHash.put(key,size);
        } catch (RocksDBException e) {
            log.warn("Load RockDB Store Hash error,cause={}",e);
        }
    }

    private byte[] getMetaKey(String key){
        return SerializeHelper.serialize(key);
    }
    private byte[] getMetaValue(Object metaValue){
        return SerializeHelper.serialize(metaValue);
    }

    private byte[] getDataKey(String key,String field){
        return SerializeHelper.serialize(key + separator + field);
    }

    private byte[] getDataValue(Object dataValue){
        return SerializeHelper.serialize(dataValue);
    }
}
