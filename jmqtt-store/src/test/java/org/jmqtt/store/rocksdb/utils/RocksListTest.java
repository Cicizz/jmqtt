package org.jmqtt.store.rocksdb.utils;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.store.rocksdb.RocksdbDao;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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
    public void size() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(100);
        String testKey = "testListKey";
        long beginSize = rocksList.size(testKey);
        System.out.println("begin size:" + beginSize);
        CountDownLatch latch = new CountDownLatch(100);
        for(int i = 0; i < 100; i++){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 100; i++){
                        Message message = new Message();
                        rocksList.add(testKey, SerializeHelper.serialize(message));
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        long endSize = rocksList.size(testKey);
        System.out.println("end size:" + endSize);
        assert( beginSize + 100*100) == endSize;
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

    @Test
    public void clear() {
        String key = "listKey";
        String value = "listValue";
        int len = 100;
        for(int i = 0 ; i < len; i++){
            this.rocksList.add(key,SerializeHelper.serialize(value));
        }
        this.rocksList.clear(key);
        long size = this.rocksList.size(key);
        assert size == 0;
        for(int i = 0; i < len; i++){
            byte[] tempValue = this.rocksList.get(key,i);
            assert tempValue == null;
        }
    }
}