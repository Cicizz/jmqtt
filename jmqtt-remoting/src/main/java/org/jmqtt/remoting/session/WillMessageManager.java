package org.jmqtt.remoting.session;

import org.jmqtt.common.bean.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WillMessageManager {

    private Map<String /* clientId */, Message/* will message */ > willMessageCache = new ConcurrentHashMap<>();

    private static final WillMessageManager INSTANCE = new WillMessageManager();

    public static WillMessageManager getInstance(){
        return INSTANCE;
    }

    public void pubWillMessage(String clientId){
        if(containWill(clientId)){
            //TODO  pub will Msg
        }
    }

    public boolean containWill(String clientId){
        return this.willMessageCache.containsKey(clientId);
    }

    public boolean hasWill(String clientId){
        return this.willMessageCache.containsKey(clientId);
    }

    public Message storeWill(String clientId,Message message){
        return this.willMessageCache.put(clientId,message);
    }

    public Message getAndRemoveWill(String clientId){
        return this.willMessageCache.remove(clientId);
    }

    public void removeWill(String clientId){
        this.willMessageCache.remove(clientId);
    }
}
