package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class RocksHash<T,K> extends AbstractRocksHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private Map<String,MetaHash> metaHashMap = new ConcurrentHashMap<>();

    private RocksDB rocksDB;

    public RocksHash(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public K get(String key,T field){
        if(!metaHashMap.containsKey(key)){
            return null;
        }
        try {
            return (K)SerializeHelper.deserialize(rocksDB.get((key+separator+field).getBytes()),Object.class);
        } catch (RocksDBException e) {
            log.warn("RockDB get Hash error,cause={}",e);
        }
        return null;
    }

    public void put(String key,T field,K value){
        try {
            if(!metaHashMap.containsKey(key)){
                synchronized (this){
                    if(!metaHashMap.containsKey(key)){
                        byte[] isExists = rocksDB.get(SerializeHelper.serialize(key));
                        MetaHash metaHash = null;
                        if(isExists != null){
                            String metaValue = SerializeHelper.deserialize(isExists,String.class);
                            long hashSize = Long.parseLong( metaValue.substring(0,metaValue.indexOf(separator)));
                            long timeStamp = Long.parseLong( metaValue.substring(metaValue.indexOf(separator)+1));
                            metaHash = new MetaHash(key,hashSize,timeStamp);
                        }else{
                            metaHash = new MetaHash(key,1L,System.currentTimeMillis());
                            String metaValue = "" + metaHash.getHashSize() + separator + metaHash.getTimeStamp();
                            this.rocksDB.put(SerializeHelper.serialize(key),SerializeHelper.serialize(metaValue));
                        }
                        metaHashMap.put(key,metaHash);
                        this.rocksDB.put((key+separator+field).getBytes(),SerializeHelper.serialize(value));
                    }
                }
            }else{
                MetaHash metaHash = metaHashMap.get(key);
                metaHash.addHashSize();
                metaHash.setTimeStamp(System.currentTimeMillis());
                String metaValue = "" + metaHash.getHashSize() + separator + metaHash.getTimeStamp();
                this.rocksDB.put((key+separator+field).getBytes(),SerializeHelper.serialize(value));
                this.rocksDB.put(SerializeHelper.serialize(key),SerializeHelper.serialize(metaValue));
            }
        } catch (RocksDBException e) {
            log.warn("RockDB store Hash error,cause={}",e);
        }
    }





    class MetaHash{
        String key;

        AtomicLong hashSize;
        AtomicReference<Long> timeStamp;

        public MetaHash(String key,long hashSize,long timeStamp){
            this.key = key;
            this.hashSize = new AtomicLong(hashSize);
            this.timeStamp = new AtomicReference(timeStamp);
        }

        public String getKey() {
            return key;
        }


        public long getHashSize() {
            return hashSize.get();
        }

        public long addHashSize() {
            return this.hashSize.incrementAndGet();
        }

        public long getTimeStamp() {
            return timeStamp.get();
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp.set(timeStamp);
        }

    }

}
