package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.store.rocksdb.RocksdbDao;
import org.junit.Before;
import org.junit.Test;

public class RocksListTest {

    private RocksList  rocksList;

    @Before
    public void before() throws Exception {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setRocksDbPath("db");
        RocksdbDao rocksdbDao = new RocksdbDao(storeConfig);
        this.rocksList = rocksdbDao.getRocksList();
    }

    @Test
    public void getFromLast() {

    }

    @Test
    public void size() {
    }

    @Test
    public void get() {
        Message message = new Message();
        rocksList.add("testClient", SerializeHelper.serialize(message));
        Message message1 = SerializeHelper.deserialize(rocksList.get("testClient",0),Message.class);
        assert  message.equals(message1);
    }

    @Test
    public void add() {
        Message message = new Message();
        rocksList.add("testClient",SerializeHelper.serialize(message));
    }
}