package org.jmqtt.store.rocksdb.db;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class RDB {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private final DBOptions DB_OPTIONS = new DBOptions();
    private final ReadOptions READ_OPTIONS = new ReadOptions();
    private final WriteOptions WRITE_OPTIONS_SYNC = new WriteOptions();
    private final WriteOptions WRITE_OPTIONS_ASYNC = new WriteOptions();
    private final BloomFilter BLOOM_FILTER = new BloomFilter();
    private final BlockBasedTableConfig BLOCK_BASED_TABLE_CONFIG = new BlockBasedTableConfig();
    private final ColumnFamilyOptions COLUMN_FAMILY_OPTIONS = new ColumnFamilyOptions();
    private final List<CompressionType> COMPRESSION_TYPES = new ArrayList();
    private StoreConfig storeConfig;
    private RocksDB DB;
    private Map<String,ColumnFamilyHandle> CF_HANDLES = new HashMap<>();

    public RDB(StoreConfig storeConfig){
        this.storeConfig = storeConfig;
    }

    public RocksIterator newIterator(ColumnFamilyHandle cfh){
        return this.DB.newIterator(cfh,READ_OPTIONS);
    }

    public List<byte[]> getByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey){
        List<byte[]> values = new ArrayList<>();
        try{
            RocksIterator iterator = this.newIterator(cfh);
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
                values.add(iterator.value());
            }
            log.debug("[RocksDB] -> succ while get by prefix,columnFamilyHandle:{}, prefixKey:{}",cfh.toString(),new String(prefixKey));
        }catch(Exception e){
            log.error("[RocksDB] ->  error while get by prefix, columnFamilyHandle:{}, prefixKey:{}, err:{}",
                    cfh.toString(), new String(prefixKey), e);
        }
        return values;
    }

    public boolean deleteByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey){
        try{
            RocksIterator iterator = this.newIterator(cfh);
            int item = 0;
            WriteBatch writeBatch = new WriteBatch();
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
                writeBatch.delete(cfh,iterator.key());
                item++;
            }
            if(item > 0){
                this.DB.write(WRITE_OPTIONS_ASYNC,writeBatch);
            }
            log.debug("[RocksDB] -> succ while delete by prefix,columnFamilyHandle:{}, prefixKey:{}, nums:{}",cfh.toString(),new String(prefixKey),item);
        }catch(RocksDBException e){
            log.error("[RocksDB] ->  error while delete by prefix, columnFamilyHandle:{}, prefixKey:{}, err:{}",
                    cfh.toString(), new String(prefixKey), e);
            return false;
        }
        return true;
    }

    public boolean deleteRange(final ColumnFamilyHandle cfh,final byte[] beginKey,final byte[] endKey){
        try {
            DB.deleteRange(cfh, beginKey, endKey);
            log.debug("[RocksDB] -> succ delete range, columnFamilyHandle:{}, beginKey:{}, endKey:{}",
                    cfh.toString(), new String(beginKey), new String(endKey));
        } catch (RocksDBException e) {
            log.error("[RocksDB] ->  error while delete range, columnFamilyHandle:{}, beginKey:{}, endKey:{}, err:{}",
                    cfh.toString(), new String(beginKey), new String(endKey), e.getMessage(), e);
            return false;
        }
        return true;
    }

    public byte[] get(final ColumnFamilyHandle cfh,final byte[] key){
        try {
            return DB.get(cfh, key);
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while get, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.toString(), new String(key), e.getMessage(), e);
            return null;
        }
    }

    public boolean writeAsync(final WriteBatch writeBatch){
        return this.write(WRITE_OPTIONS_ASYNC,writeBatch);
    }

    public boolean writeSync(final WriteBatch writeBatch){
        return this.write(WRITE_OPTIONS_SYNC,writeBatch);
    }

    public boolean write(final WriteOptions writeOptions,final WriteBatch writeBatch){
        try {
            this.DB.write(writeOptions,writeBatch);
            log.debug("[RocksDB] -> success write writeBatch, size:{}", writeBatch.count());
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while write batch, err:{}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean putSync(final ColumnFamilyHandle cfh,final byte[] key,final byte[] value){
        return this.put(cfh,WRITE_OPTIONS_ASYNC,key,value);
    }

    public boolean delete(final ColumnFamilyHandle cfh, final byte[] key) {
        try {
            this.DB.delete(cfh, key);
            log.debug("[RocksDB] -> succ delete key, columnFamilyHandle:{}, key:{}", cfh.toString(), new String(key));
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> while delete key, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.toString(), new String(key), e.getMessage(), e);
        }
        return true;
    }

    public boolean put(final ColumnFamilyHandle cfh,final WriteOptions writeOptions,final byte[] key,final byte[] value){
        try {
            this.DB.put(cfh, writeOptions, key, value);
            log.debug("[RocksDB] -> success put value");
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while put, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.isOwningHandle(), new String(key), e.getMessage(), e);
            return false;
    }
        return true;
    }

    public void init(){
        this.DB_OPTIONS.setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                .setMaxBackgroundFlushes(this.storeConfig.getMaxBackgroundFlushes())
                .setMaxBackgroundCompactions(this.storeConfig.getMaxBackgroundCompactions())
                .setMaxOpenFiles(this.storeConfig.getMaxOpenFiles())
                .setRowCache(new LRUCache(1024* SizeUnit.MB,16,true,5))
                .setMaxSubcompactions(this.storeConfig.getMaxSubcompactions());
        this.DB_OPTIONS.setBaseBackgroundCompactions(this.storeConfig.getBaseBackGroundCompactions());
        READ_OPTIONS.setPrefixSameAsStart(true);
        WRITE_OPTIONS_SYNC.setSync(true);
        WRITE_OPTIONS_ASYNC.setSync(false);
        BLOCK_BASED_TABLE_CONFIG.setFilter(BLOOM_FILTER)
                .setCacheIndexAndFilterBlocks(true)
                .setPinL0FilterAndIndexBlocksInCache(true);

        COMPRESSION_TYPES.addAll(Arrays.asList(
                CompressionType.NO_COMPRESSION,CompressionType.NO_COMPRESSION,
                CompressionType.LZ4_COMPRESSION,CompressionType.LZ4_COMPRESSION,
                CompressionType.LZ4_COMPRESSION,CompressionType.ZSTD_COMPRESSION,
                CompressionType.ZSTD_COMPRESSION
        ));

        COLUMN_FAMILY_OPTIONS.setTableFormatConfig(BLOCK_BASED_TABLE_CONFIG)
                .useFixedLengthPrefixExtractor(this.storeConfig.getUseFixedLengthPrefixExtractor())
                .setWriteBufferSize(this.storeConfig.getWriteBufferSize() * SizeUnit.MB)
                .setMaxWriteBufferNumber(this.storeConfig.getMaxWriteBufferNumber())
                .setLevel0SlowdownWritesTrigger(this.storeConfig.getLevel0SlowdownWritesTrigger())
                .setLevel0StopWritesTrigger(10)
                .setCompressionPerLevel(COMPRESSION_TYPES)
                .setTargetFileSizeBase(this.storeConfig.getTargetFileSizeBase() * SizeUnit.MB)
                .setMaxBytesForLevelBase(this.storeConfig.getMaxBytesForLevelBase() * SizeUnit.MB)
                .setOptimizeFiltersForHits(true);

        long start = System.currentTimeMillis();
        boolean result = MixAll.createIfNotExistsDir(new File(storeConfig.getRocksDbPath()));
        assert result;
        try {
            List<ColumnFamilyDescriptor> cfDescriptors = getCFDescriptors();
            List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
            DB = RocksDB.open(DB_OPTIONS,storeConfig.getRocksDbPath(),cfDescriptors,cfHandles);
            cacheCFHandles(cfHandles);
            long end = System.currentTimeMillis();
            log.info("[RocksDB] -> start RocksDB success,consumeTime:{}",(end-start));
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> init RocksDB error,ex:{}",e);
            System.exit(-1);
        }

    }

    private List<ColumnFamilyDescriptor> getCFDescriptors(){
        List<ColumnFamilyDescriptor> list = new ArrayList<>();
        list.add(new ColumnFamilyDescriptor("default".getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.SESSION.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.SUBSCRIPTION.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.WILL_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.OFFLINE_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.REC_FLOW_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.SEND_FLOW_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(RDBStorePrefix.RETAIN_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        return list;
    }

    private void cacheCFHandles(List<ColumnFamilyHandle> cfHandles) throws RocksDBException {
        if(cfHandles == null || cfHandles.size() == 0){
            log.error("[RocksDB] -> init columnFamilyHandle failure.");
            throw new RocksDBException("init columnFamilyHandle failure");
        }
        for (ColumnFamilyHandle cfHandle : cfHandles) {
            this.CF_HANDLES.put(new String(cfHandle.getName()),cfHandle);
        }
    }

    public void close(){
        this.DB_OPTIONS.close();
        this.WRITE_OPTIONS_SYNC.close();
        this.WRITE_OPTIONS_ASYNC.close();
        this.READ_OPTIONS.close();
        this.COLUMN_FAMILY_OPTIONS.close();
        CF_HANDLES.forEach((x,y) -> {
            y.close();
        });
        if(DB != null){
            DB.close();
        }
    }

    public ColumnFamilyHandle getColumnFamilyHandle(String cfhName){
        return this.CF_HANDLES.get(cfhName);
    }
}
