package org.jmqtt.mqtt;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端连接管理器
 */
public class ConnectManager {

    private  Map<String /* clientId */, MQTTConnection /* ClientSession */> clientCache = new ConcurrentHashMap<>();

    private static final  ConnectManager INSTANCE  =  new ConnectManager();

    private ConnectManager(){}

    public static  ConnectManager getInstance(){
        return INSTANCE;
    }

    public Collection<MQTTConnection> getAllConnections(){
        return this.clientCache.values();
    }

    public MQTTConnection getClient(String clientId){
        return this.clientCache.get(clientId);
    }

    public MQTTConnection putClient(String clientId,MQTTConnection clientSession){
        return this.clientCache.put(clientId,clientSession);
    }

    public boolean containClient(String clientId){
        return this.clientCache.containsKey(clientId);
    }

    public MQTTConnection removeClient(String clientId){
        if (clientId != null) {
            return this.clientCache.remove(clientId);
        }
        return null;
    }

}
