package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;


public class RocksMap extends AbstractRocksHandler{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksDB rocksDB;

    public RocksMap(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public boolean remove(String key){
        try {
            rocksDB.delete(SerializeHelper.serialize(key));
            return true;
        } catch (RocksDBException e) {
            log.warn("RockDB get String error,cause={}",e);
        }
        return false;
    }

    public Collection<byte[]> values(String key){
        RocksIterator iterator = this.rocksDB.newIterator();
        Collection<byte[]> values = new ArrayList<>();
        for(iterator.seek(SerializeHelper.serialize(key));iterator.isValid();iterator.next()){
            values.add(iterator.value());
        }
        return values;
    }

    public byte[] get(String key){
        try {
            byte[] valueBytes = rocksDB.get(SerializeHelper.serialize(key));
            if(valueBytes != null){
                return valueBytes;
            }
        } catch (RocksDBException e) {
            log.warn("RockDB get String error,cause={}",e);
        }
        return null;
    }

    public boolean contains(String key){
        try {
            byte[] valueBytes = rocksDB.get(SerializeHelper.serialize(key));
            return valueBytes == null ? false : true;
        } catch (RocksDBException e) {
            log.warn("RockDB get String error,cause={}",e);
        }
        return false;
    }

    public void set(String key,byte[] value){
        byte[] keyByte = SerializeHelper.serialize(key);
        try {
            rocksDB.put(keyByte,value);
        } catch (RocksDBException e) {
            log.warn("RockDB store String error,cause={}",e);
        }
    }

}
