package org.jmqtt.store.rocksdb.vo;

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


public class RocksHash extends AbstractRocksHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private Map<String,MetaHash> metaHashMap = new ConcurrentHashMap<>();

    private RocksDB rocksDB;

    public RocksHash(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public void put(String key,String field,byte[] value){
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
                            metaHash = new MetaHash(key,0l,System.currentTimeMillis());
                        }
                        metaHashMap.put(key,metaHash);
                    }
                }
            }else{
                MetaHash metaHash = metaHashMap.get(key);
                metaHash.addHashSize();
                metaHash.setTimeStamp(System.currentTimeMillis());
                this.rocksDB.put((key+separator+field).getBytes(),value);
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

    class DataHash{
        byte[] key;
        byte[] field;
        byte[] value;

        public DataHash(byte[] key, byte[] field, byte[] value) {
            this.key = key;
            this.field = field;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getField() {
            return field;
        }


        public byte[] getValue() {
            return value;
        }

    }

}
