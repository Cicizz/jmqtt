package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.OfflineMessageStore;

import java.util.Collection;

public class RedisOfflineMessageStore implements OfflineMessageStore {

    private RedisStoreUtil offlineTable;
    private int msgMaxNum = 1000;

    public RedisOfflineMessageStore(StoreConfig redisConfig){
        this.offlineTable = new RedisStoreUtil(redisConfig,"offlineTable:");
    }

    @Override
    public void clearOfflineMsgCache(String clientId) {
        offlineTable.delete(clientId);
    }

    @Override
    public boolean containOfflineMsg(String clientId) {
        return offlineTable.scontain(clientId);
    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        return offlineTable.laddMsg(msgMaxNum,clientId,message);
    }

    @Override
    public Collection<Message> getAllOfflineMessage(String clientId) {
        return offlineTable.lgetAllMsg(clientId,msgMaxNum);
    }
}
