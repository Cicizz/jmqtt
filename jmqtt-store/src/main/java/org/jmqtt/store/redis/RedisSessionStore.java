package org.jmqtt.store.redis;

import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.SessionStore;

public class RedisSessionStore implements SessionStore {

    private RedisStoreUtil sessionTable;

    public RedisSessionStore(RedisConfig redisConfig){
        this.sessionTable = new RedisStoreUtil(redisConfig,"sessionTable:");
    }

    @Override
    public boolean containSession(String clientId) {
        return sessionTable.scontain(clientId);
    }

    @Override
    public Object setSession(String clientId, Object obj) {
        return sessionTable.<Object>sgetSetMsg(clientId,obj);
    }

    @Override
    public Object getLastSession(String clientId) {
        return sessionTable.<Object>sgetMsg(clientId,Object.class);
    }

    @Override
    public boolean clearSession(String clientId) {
        sessionTable.delete(clientId);
        return true;
    }
}
