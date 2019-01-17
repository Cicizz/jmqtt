package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.store.rocksdb.RocksdbDao;
import org.jmqtt.store.rocksdb.RocksdbStorePrefix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Collection;

import static org.junit.Assert.*;

public class RocksMapTest {

    private RocksMap  rocksMap;

    @Before
    public void before() throws Exception {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setRocksDbPath("db");
        RocksdbDao rocksdbDao = new RocksdbDao(storeConfig);
        this.rocksMap = rocksdbDao.getRocksMap();
    }

    @Test
    public void get() {
        rocksMap.set("testKey","testValue".getBytes());
        String value = SerializeHelper.deserialize(rocksMap.get("testKey"),String.class);
        assert "testValue".equals(value);
    }

    @Test
    public void contains() {
        rocksMap.set("testKey","testValue".getBytes());
        boolean value = rocksMap.contains("testKey");
        assert value;
    }

    @Test
    public void set() {
        rocksMap.set("testKey","testValue".getBytes());
    }

    @Test
    public void values() {
        String key = RocksdbStorePrefix.SESSION+"paho-295665362751";
        String key2 = RocksdbStorePrefix.SESSION+"paho-231312312321";
//        Message message = new Message();
        String message = "aaaaaaaaa";
        this.rocksMap.set(key,SerializeHelper.serialize(message));
        this.rocksMap.set(key2,SerializeHelper.serialize(message));
        Collection<byte[]> values = this.rocksMap.values(RocksdbStorePrefix.RETAIN_MESSAGE);
        for(byte[] item : values){
            System.out.println(SerializeHelper.deserialize(item,Object.class));
        }
        if(values.size() > 0){
            assert false;
        }
    }

    @Test
    public void values2() {
        String key = RocksdbStorePrefix.SUBSCRIPTION+"paho-295665362751";
//        Message message = new Message();
        String message = "bbbbbbbbbbbbb";
        this.rocksMap.set(key,SerializeHelper.serialize(message));
        Collection<byte[]> values = this.rocksMap.values(RocksdbStorePrefix.RETAIN_MESSAGE);
        for(byte[] item : values){
            System.out.println(SerializeHelper.deserialize(item,String.class));
        }
//        if(values.size() > 0){
//            assert false;
//        }
    }
}