package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RocksMap<T,K> extends AbstractRocksHandler{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksDB rocksDB;

    public RocksMap(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public K get(T key){
        try {
            byte[] valueBytes = rocksDB.get(SerializeHelper.serialize(key));
            if(valueBytes != null){
                return (K)SerializeHelper.deserialize(valueBytes,Object.class);
            }
        } catch (RocksDBException e) {
            log.warn("RockDB get String error,cause={}",e);
        }
        return null;
    }

    public void set(T key,K value){
        byte[] keyByte = SerializeHelper.serialize(key);
        byte[] valByte = SerializeHelper.serialize(value);
        try {
            rocksDB.put(keyByte,valByte);
        } catch (RocksDBException e) {
            log.warn("RockDB store String error,cause={}",e);
        }
    }

}
