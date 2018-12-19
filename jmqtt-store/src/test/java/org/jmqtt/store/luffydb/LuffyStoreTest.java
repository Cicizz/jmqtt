package org.jmqtt.store.luffydb;

import org.junit.Before;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.util.SizeUnit;

import static org.junit.Assert.*;

public class LuffyStoreTest {

    private RocksDB rocksDB;

    @Before
    public void before(){
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true)
                .setWriteBufferSize(64 * SizeUnit.KB)
                .setMaxWriteBufferNumber(3)
                .setMaxBackgroundCompactions(10);
    }

}