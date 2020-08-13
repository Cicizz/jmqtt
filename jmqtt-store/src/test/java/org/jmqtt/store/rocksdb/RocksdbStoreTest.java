package org.jmqtt.store.rocksdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

public class RocksdbStoreTest {

    private RocksDB rocksDB;
    private char sep = 1;

    @Before
    public void before(){
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true)
                .setWriteBufferSize(64 * SizeUnit.KB)
                .setMaxWriteBufferNumber(3)
                .setMaxBackgroundCompactions(10)
                .setCompressionType(CompressionType.NO_COMPRESSION)
                .setCompactionStyle(CompactionStyle.UNIVERSAL);
        Filter bloomFilter = new BloomFilter(100);
        ReadOptions readOptions = new ReadOptions().setFillCache(false);
        RateLimiter rateLimiter = new RateLimiter(10000000, 10000, 10);

        options.setMemTableConfig(
                new HashSkipListMemTableConfig()
                        .setHeight(4)
                        .setBranchingFactor(4)
                        .setBucketCount(2000000));

        options.setMemTableConfig(
                new HashLinkedListMemTableConfig()
                        .setBucketCount(100000));
        options.setMemTableConfig(
                new VectorMemTableConfig().setReservedSize(10000));

        options.setMemTableConfig(new SkipListMemTableConfig());

        options.setTableFormatConfig(new PlainTableConfig());
        // Plain-Table requires mmap read
        options.setAllowMmapReads(true);

        options.setRateLimiter(rateLimiter);
        final BlockBasedTableConfig table_options = new BlockBasedTableConfig();
        table_options.setBlockCacheSize(64 * SizeUnit.KB)
                .setFilter(bloomFilter)
                .setCacheNumShardBits(6)
                .setBlockSizeDeviation(5)
                .setBlockRestartInterval(10)
                .setCacheIndexAndFilterBlocks(true)
                .setHashIndexAllowCollision(false)
                .setBlockCacheCompressedSize(64 * SizeUnit.KB)
                .setBlockCacheCompressedNumShardBits(10);

        options.setTableFormatConfig(table_options);
        try {
            rocksDB = RocksDB.open(options,"db");
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMap() throws RocksDBException {
        String key = "key1";
        String value = "this is a test value";
        rocksDB.put(key.getBytes(),value.getBytes());
        String getValue = new String(rocksDB.get(key.getBytes()));
        Assert.assertTrue(getValue.equals(value));
    }

    @Test
    public void testHash() throws RocksDBException {
        String key = "hashKey";
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        String value2 = "value2";
        hset(key,field1,value1);
        hset(key,field2,value2);
        RocksIterator iterator = rocksDB.newIterator();
        for(iterator.seek(key.getBytes());iterator.isValid();iterator.next()){
            System.out.println(new String(iterator.key()) + "=====" + new String(iterator.value()));
        }
    }


    private void hset(String key,String field,String value) throws RocksDBException {
        String relKey = key + sep + field;
        rocksDB.put(relKey.getBytes(),value.getBytes());
    }

    private void hList(String key,String value) throws RocksDBException {
        rocksDB.put((key + sep + value).getBytes(),value.getBytes());
    }

    @Test
    public void testList() throws RocksDBException {
        String listKey = "listKey";
        for(int i = 0 ; i < 10; i++){
            long rs = rocksDB.getLatestSequenceNumber();
            System.out.println(rs);
            byte[] byteKey = (listKey + sep + rs).getBytes();
            rocksDB.put(byteKey,("val"+i).getBytes());
        }
        RocksIterator iterator = rocksDB.newIterator();

        for(iterator.seek(listKey.getBytes());iterator.isValid();iterator.next()){
            System.out.println(new String(iterator.key()) + "========" + new String(iterator.value()));
        }
    }




}