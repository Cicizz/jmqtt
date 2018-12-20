package org.jmqtt.store.redis;

import net.sf.json.JSONObject;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.redis.RedisDao;
import org.jmqtt.store.redis.RedisStoreManager;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.Collection;

public class RedisStoreUtil implements RedisDao {
    private JSONObject jsonObject;
    private RedisConfig redisConfig;
    private RedisStoreManager redisStoreManager;
    private JedisCluster cluster;
    private String keyName;
    public RedisStoreUtil(RedisConfig Config,String keyName){
        this.redisConfig = Config;
        this.redisStoreManager = RedisStoreManager.getInstance(redisConfig);
        this.cluster = redisStoreManager.getCluster();
        this.keyName = keyName;
    }
    @Override
    public void delete(String clientId) {
        cluster.del(keyName+clientId);
    }

    @Override
    public Message getMsg(String clientId, Integer msgId) {
        jsonObject = JSONObject.fromObject(cluster.hget(keyName+clientId,String.valueOf(msgId)));
        return (Message)JSONObject.toBean(jsonObject,Message.class);
    }

    @Override
    public boolean storeMsg(String clientId, Message message) {
        jsonObject = JSONObject.fromObject(message);
        cluster.hset(keyName+clientId,String.valueOf(message.getMsgId()),jsonObject.toString());
        return true;
    }

    @Override
    public Message releaseMsg(String clientId, int msgId) {
        Message message = this.getMsg(clientId,msgId);
        cluster.hdel(keyName+clientId,String.valueOf(msgId));
        return message;
    }

    @Override
    public Collection<Message> getAllMsg(String clientId) {
        ArrayList<Message> messagesList = new ArrayList<>();
        if (cluster.exists(keyName+clientId)){
            for (String temp:cluster.hvals(keyName+clientId)){
                messagesList.add((Message) JSONObject.toBean(JSONObject.fromObject(temp),Message.class));
            }
           return messagesList;
        }
        return messagesList;
    }

    @Override
    public boolean containMsg(String clientId, int msgId) {
        return cluster.hexists(keyName+clientId,String.valueOf(msgId));
    }

}
