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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RocksHashTest {

    private RocksHash  rocksHash;

    @Before
    public void before() throws Exception {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setRocksDbPath("db");
        RocksdbDao rocksdbDao = new RocksdbDao(storeConfig);
        this.rocksHash = rocksdbDao.getRocksHash();
    }

    @Test
    public void get(){
        String key = "testHash";
        String field = "testField";
        Message message = new Message();
        rocksHash.put(key,field, SerializeHelper.serialize(message));
        byte[] x = rocksHash.get(key,field);
        assert SerializeHelper.deserialize(x,Message.class).equals(message);
    }

    @Test
    public void size() throws InterruptedException {
        String key = "testHash2";
        long beginSize = rocksHash.size(key);
        System.out.println(beginSize);
        CountDownLatch latch = new CountDownLatch(1000);
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        for(int i = 0; i < 100 ;i++){
            final AtomicInteger count = new AtomicInteger(0);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for(int j = 0; j < 1000; j++){
                        String field = "testField"+ j * count.get();
                        Message message = new Message();
                        rocksHash.put(key,field,SerializeHelper.serialize(message));
                        long size = rocksHash.size(key);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        long size = rocksHash.size(key);
        System.out.println(size);
        assert size == (beginSize + 100*1000);
    }

    @Test
    public void put() {
        String key = "testHash";
        String field = "testField";
        Message message = new Message();
        rocksHash.put(key,field, SerializeHelper.serialize(message));
    }
}