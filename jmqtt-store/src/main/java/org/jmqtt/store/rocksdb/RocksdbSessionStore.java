package org.jmqtt.store.rocksdb;

import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.SessionStore;
import org.jmqtt.store.rocksdb.utils.RocksMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksdbDao rocksdbDao;
    private RocksMap rocksMap;

    public RocksdbSessionStore(RocksdbDao rocksdbDao){
        this.rocksdbDao = rocksdbDao;
        this.rocksMap = rocksdbDao.getRocksMap();
    }

    @Override
    public boolean containSession(String clientId) {
        return rocksMap.contains(RocksdbStorePrefix.SESSION + clientId);
    }

    @Override
    public Object setSession(String clientId, Object obj) {
        rocksMap.set(RocksdbStorePrefix.SESSION + clientId, SerializeHelper.serialize(obj));
        return obj;
    }

    @Override
    public Object getLastSession(String clientId) {
        byte[] sessionBytes = rocksMap.get(RocksdbStorePrefix.SESSION + clientId);
        if(sessionBytes != null){
            return SerializeHelper.deserialize(sessionBytes,Object.class);
        }
        return null;
    }

    @Override
    public boolean clearSession(String clientId) {
        return rocksMap.remove(RocksdbStorePrefix.SESSION + clientId);
    }
}
