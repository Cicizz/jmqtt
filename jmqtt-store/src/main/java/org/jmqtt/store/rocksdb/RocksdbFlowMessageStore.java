package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.rocksdb.utils.RocksHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class RocksdbFlowMessageStore implements FlowMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksHash rocksHash;

    public RocksdbFlowMessageStore(RocksHash rocksHash){
        this.rocksHash = rocksHash;
    }

    @Override
    public void clearClientFlowCache(String clientId) {
        this.rocksHash.clear(RocksdbStorePrefix.REC_FLOW_MESSAGE + clientId);
        this.rocksHash.clear(RocksdbStorePrefix.SEND_FLOW_MESSAGE + clientId);
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        byte[] value = this.rocksHash.get(RocksdbStorePrefix.REC_FLOW_MESSAGE + clientId,String.valueOf(msgId));
        if(value == null){
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        try{
            this.rocksHash.put(RocksdbStorePrefix.REC_FLOW_MESSAGE+clientId,String.valueOf(message.getMsgId()),SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Cache Recive message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        byte[] value = this.rocksHash.get(RocksdbStorePrefix.REC_FLOW_MESSAGE+clientId,String.valueOf(msgId));
        if(value == null){
            log.warn("The message is not exist,clientId={},msgId={}",clientId,msgId);
            return null;
        }
        Message returnMessage = SerializeHelper.deserialize(value,Message.class);
        this.rocksHash.remove(RocksdbStorePrefix.REC_FLOW_MESSAGE+clientId,String.valueOf(msgId));
        return returnMessage;
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        try{
            this.rocksHash.put(RocksdbStorePrefix.SEND_FLOW_MESSAGE+clientId,String.valueOf(message.getMsgId()),SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Cache Send message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        Collection<byte[]> values = this.rocksHash.values(RocksdbStorePrefix.SEND_FLOW_MESSAGE + clientId);
        Collection<Message> messages = new ArrayList<>();
        for(byte[] value : values){
            messages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return messages;
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        try{
            this.rocksHash.remove(RocksdbStorePrefix.SEND_FLOW_MESSAGE+clientId,String.valueOf(msgId));
            return true;
        }catch (Exception ex){
            log.warn("Cache Send message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return this.rocksHash.contains(RocksdbStorePrefix.SEND_FLOW_MESSAGE + clientId,String.valueOf(msgId));
    }
}
