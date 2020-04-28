package org.jmqtt.store.memory;

import org.jmqtt.common.model.Message;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.OfflineMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOfflineMessageStore implements OfflineMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private Map<String,BlockingQueue<Message>> offlineTable = new ConcurrentHashMap<>();
    private int msgMaxNum = 1000;

    public DefaultOfflineMessageStore(){
    }

    @Override
    public void clearOfflineMsgCache(String clientId) {
        this.offlineTable.remove(clientId);
    }

    @Override
    public boolean containOfflineMsg(String clientId) {
        return offlineTable.containsKey(clientId);
    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        if(!this.offlineTable.containsKey(clientId)){
            synchronized (offlineTable){
                if(!offlineTable.containsKey(clientId)){
                    BlockingQueue<Message> queue = new ArrayBlockingQueue(1000);
                    offlineTable.put(clientId,queue);
                }
            }
        }
        BlockingQueue<Message> queue = this.offlineTable.get(clientId);
        while(queue.size() > msgMaxNum){
            try {
                queue.take();
            } catch (InterruptedException e) {
                log.warn("[StoreOfflineMessage] -> Store Offline message error,clientId={},msgId={}",clientId,message.getMsgId());
            }
        }
        queue.offer(message);
        return true;
    }

    @Override
    public Collection<Message> getAllOfflineMessage(String clientId) {
        BlockingQueue<Message> queue = offlineTable.get(clientId);
        Collection<Message> allMessages = new ArrayList<>();
        int rs = queue.drainTo(allMessages);
        return allMessages;
    }
}
