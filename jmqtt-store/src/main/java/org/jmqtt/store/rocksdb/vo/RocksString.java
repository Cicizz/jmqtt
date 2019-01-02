package org.jmqtt.store.rocksdb.vo;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class RocksString extends AbstractRocksHandler{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksDB rocksDB;

    public RocksString(RocksDB rocksDB){
        this.rocksDB = rocksDB;
    }

    public String get(String key){
        String rs = null;
        try {
            byte[] valueBytes = rocksDB.get(SerializeHelper.serialize(key));
            rs = valueBytes == null ? "" : new String(valueBytes, Charset.forName("utf-8"));
        } catch (RocksDBException e) {
            log.warn("RockDB get String error,cause={}",e);
        }
        return rs;
    }

    public void set(String key,String value){
        byte[] keyByte = SerializeHelper.serialize(key);
        byte[] valByte = SerializeHelper.serialize(value);
        try {
            rocksDB.put(keyByte,valByte);
        } catch (RocksDBException e) {
            log.warn("RockDB store String error,cause={}",e);
        }
    }
    class StringVO{
        String key;
        String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
