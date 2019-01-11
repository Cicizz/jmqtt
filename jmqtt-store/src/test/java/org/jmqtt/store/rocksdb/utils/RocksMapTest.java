package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.store.rocksdb.RocksdbDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

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
}