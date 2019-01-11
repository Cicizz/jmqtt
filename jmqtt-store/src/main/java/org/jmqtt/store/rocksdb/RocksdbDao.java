package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.rocksdb.utils.RocksHash;
import org.jmqtt.store.rocksdb.utils.RocksList;
import org.jmqtt.store.rocksdb.utils.RocksMap;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbDao {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksDB rocksDB;
    private String rocksDbPath;

    private RocksList rocksList;
    private RocksHash rocksHash;
    private RocksMap rocksMap;

    public RocksdbDao(StoreConfig storeConfig) throws Exception {
        RocksDB.loadLibrary();
        this.rocksDbPath = storeConfig.getRocksDbPath();
        init();
    }

    public void init() throws Exception {
        Options options = new Options();
        options.setCreateIfMissing(true)
                .setWriteBufferSize(64 * SizeUnit.KB)
                .setMaxWriteBufferNumber(3)
                .setMaxBackgroundCompactions(10)
                .setCompressionType(CompressionType.NO_COMPRESSION)
                .setCompactionStyle(CompactionStyle.UNIVERSAL);
        Filter bloomFilter = new BloomFilter(100);
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
            rocksDB = RocksDB.open(options,rocksDbPath);
            this.rocksList = new RocksList(rocksDB);
            this.rocksHash = new RocksHash(rocksDB);
            this.rocksMap = new RocksMap(rocksDB);
        } catch (RocksDBException e) {
            log.error("Initialize rocksdb failure.cause = {}",e);
            throw new Exception("Initialize Rocksdb StoreException");
        }
    };

    public void close(){
        this.rocksDB.close();
    }

    public RocksList getRocksList() {
        return rocksList;
    }

    public RocksHash getRocksHash() {
        return rocksHash;
    }

    public RocksMap getRocksMap() {
        return rocksMap;
    }
}
