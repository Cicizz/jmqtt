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
    public void getFromLast() {

    }

    @Test
    public void size() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicLong lastSize = new AtomicLong(0);
        for(int i = 0; i < 10; i++){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 10; i++){
                        Message message = new Message();
                        rocksList.add("testClient", SerializeHelper.serialize(message));
                        long size = rocksList.size("testClient");
                        lastSize.incrementAndGet();
                        System.out.println(size);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        assert lastSize.get() == 100;
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