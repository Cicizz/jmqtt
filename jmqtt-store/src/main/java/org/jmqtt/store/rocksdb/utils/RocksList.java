package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RocksList<T> extends AbstractRocksHandler{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);
    private RocksDB rocksDB;
    private Map<String,MetaList> metaListMap = new ConcurrentHashMap<>();

    public RocksList(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public T getFromLast(String key){
        if(!metaListMap.containsKey(key)){
            throw new RuntimeException("The key of list is not exist!");
        }
        return get(key,metaListMap.get(key).getListSize());
    }

    public T get(String key,long index){
        if(!metaListMap.containsKey(key)){
            throw new RuntimeException("The key of list is not exist!");
        }
        if(metaListMap.get(key).getListSize() < index){
            throw new RuntimeException("The index is over the list size");
        }
        String relKey = key + separator + index;
        try {
            byte[] value = rocksDB.get(SerializeHelper.serialize(relKey));
            if(value == null){
                return null;
            }
            return (T)SerializeHelper.deserialize(value,Object.class);
        } catch (RocksDBException e) {
            log.warn("RockDB get last value from List error,cause={}",e);
        }
        return null;

    }

    public void add(String key,T value){
        try{
            if(!metaListMap.containsKey(key)){
                synchronized (this){
                    if(!metaListMap.containsKey(key)){
                        byte[] isExists = rocksDB.get(SerializeHelper.serialize(key));
                        MetaList metaList = null;
                        if(isExists != null){
                            String metaValue = SerializeHelper.deserialize(isExists,String.class);
                            long listSize = Long.parseLong(metaValue.substring(0,metaValue.indexOf(separator)));
                            long timeStamp = Long.parseLong( metaValue.substring(metaValue.indexOf(separator)+1));
                            metaList = new MetaList(key,listSize,timeStamp);
                        }else{
                            metaList = new MetaList(key,0L,System.currentTimeMillis());
                            String metaValue = "" + metaList.getListSize() + separator + metaList.getTimeStamp();
                            this.rocksDB.put(SerializeHelper.serialize(key),SerializeHelper.serialize(metaValue));
                        }
                        metaListMap.put(key,metaList);
                        this.rocksDB.put((key+separator+metaList.getListSize()).getBytes(),SerializeHelper.serialize(value));
                    }
                }
            }else{
                MetaList metaList = metaListMap.get(key);
                metaList.addListSize();
                metaList.setTimeStamp(System.currentTimeMillis());
                String metaValue = "" + metaList.getListSize() + separator + metaList.getTimeStamp();
                this.rocksDB.put((key+separator+metaList.getListSize()).getBytes(),SerializeHelper.serialize(value));
                this.rocksDB.put(SerializeHelper.serialize(key),SerializeHelper.serialize(metaValue));
            }
        }catch (Exception e){
            log.warn("RockDB store List error,cause={}",e);
        }
    }

    class MetaList{
        String key;

        AtomicLong listSize;
        AtomicReference<Long> timeStamp;

        public MetaList(String key,long listSize,long timeStamp){
            this.key = key;
            this.listSize = new AtomicLong(listSize);
            this.timeStamp = new AtomicReference(timeStamp);
        }

        public String getKey() {
            return key;
        }


        public long getListSize() {
            return listSize.get();
        }

        public long addListSize() {
            return this.listSize.incrementAndGet();
        }

        public long getTimeStamp() {
            return timeStamp.get();
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp.set(timeStamp);
        }

    }


}
