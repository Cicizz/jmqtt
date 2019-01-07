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

public class RocksList extends AbstractRocksHandler{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);
    private RocksDB rocksDB;
    private Map<String, AtomicLong> metaList = new ConcurrentHashMap<>();

    public RocksList(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }


    public long size(String key){
        if(!metaList.containsKey(key)){
            synchronized (this){
                if(!metaList.containsKey(key)){
                    loadMetaList(key);
                }
            }
            return metaList.get(key).get();
        }else{
            return metaList.get(key).get();
        }
    };

    public boolean contains(String key){
        try {
            byte[] isExists = rocksDB.get(getMetaKey(key));
            return isExists != null;
        } catch (RocksDBException e) {
            log.warn("Get Rocksdb list error,{}",e);
        }
        return false;
    };

    public byte[] get(String key,long sequence){
        if(!contains(key)){
            throw new RuntimeException("The key of list is not exist!");
        }
        if(metaList.get(key).get() < sequence){
            throw new RuntimeException("The index is over the list size");
        }
        try {
            byte[] value = rocksDB.get(getDataKey(key,sequence));
            if(value == null){
                return null;
            }
            return value;
        } catch (RocksDBException e) {
            log.warn("RockDB get last value from List error,cause={}",e);
        }
        return null;
    }

    public void add(String key,byte[] value){
        try{
            if(!metaList.containsKey(key)){
                synchronized (this){
                    if(!metaList.containsKey(key)){
                        loadMetaList(key);
                        long size = metaList.get(key).incrementAndGet();
                        WriteBatch writeBatch = new WriteBatch();
                        writeBatch.put(getMetaKey(key),getMetaValue(size));
                        writeBatch.put(getDataKey(key,size),value);
                        this.rocksDB.write(new WriteOptions(),writeBatch);
                        return;
                    }
                }
            }
            long size = metaList.get(key).incrementAndGet();
            WriteBatch writeBatch = new WriteBatch();
            writeBatch.put(getMetaKey(key),getMetaValue(size));
            writeBatch.put(getDataKey(key,size),value);
            this.rocksDB.write(new WriteOptions(),writeBatch);
        }catch (Exception e){
            log.warn("RockDB store List error,cause={}",e);
        }
    }

    private void loadMetaList(String key){
        try {
            byte[] isKeyExists = rocksDB.get(getMetaKey(key));
            AtomicLong sequence = null;
            if(isKeyExists == null){
                sequence = new AtomicLong(0);
            }else{
                sequence = new AtomicLong(SerializeHelper.deserialize(isKeyExists,Long.class));
            }
            this.metaList.put(key,sequence);
        } catch (RocksDBException e) {
            log.warn("Load RockDB store Hash error,cause={}",e);
        }
    }

    private byte[] getMetaKey(String key){
        return SerializeHelper.serialize(key);
    }
    private byte[] getMetaValue(Object metaValue){
        return SerializeHelper.serialize(metaValue);
    }

    private byte[] getDataKey(String key,long sequence){
        return SerializeHelper.serialize(key + separator + sequence);
    }

    private byte[] getDataValue(Object dataValue){
        return SerializeHelper.serialize(dataValue);
    }

}
