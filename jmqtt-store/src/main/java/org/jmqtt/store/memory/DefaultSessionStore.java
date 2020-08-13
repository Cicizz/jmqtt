package org.jmqtt.store.memory;

import org.jmqtt.store.SessionStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSessionStore implements SessionStore {

    private Map<String,Object> sessionTable = new ConcurrentHashMap<>();

    @Override
    public boolean containSession(String clientId) {
        return sessionTable.containsKey(clientId);
    }

    @Override
    public Object setSession(String clientId, Object obj) {
        return this.sessionTable.put(clientId,obj);
    }

    @Override
    public Object getLastSession(String clientId) {
        return sessionTable.get(clientId);
    }

    @Override
    public boolean clearSession(String clientId) {
        sessionTable.remove(clientId);
        return true;
    }
}
