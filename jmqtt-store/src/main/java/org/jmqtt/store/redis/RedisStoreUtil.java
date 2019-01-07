package org.jmqtt.store.redis;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.redis.RedisDao;
import org.jmqtt.store.redis.RedisStoreManager;
import redis.clients.jedis.*;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

public class RedisStoreUtil implements RedisDao {
    private JSONObject jsonObject;
    private RedisConfig redisConfig;
    private RedisStoreManager redisStoreManager;
    public static JedisCluster cluster;
    private String keyName;
    public RedisStoreUtil(RedisConfig Config,String keyName){
        this.redisConfig = Config;
        this.redisStoreManager = RedisStoreManager.getInstance(redisConfig);
        redisStoreManager.initialization();
        this.cluster = redisStoreManager.getCluster();
        this.keyName = "{"+keyName+":}";
    }
    @Override
    public void delete(String clientId) {
        cluster.del(keyName+clientId);
    }

    @Override
    public Message hgetMsg(String clientId, Integer msgId) {
        jsonObject = JSONObject.fromObject(cluster.hget(keyName+clientId,String.valueOf(msgId)));
        return (Message)JSONObject.toBean(jsonObject,Message.class);
    }

    @Override
    public boolean hstoreMsg(String clientId,String str,Object obj) {
        jsonObject = JSONObject.fromObject(obj);
        cluster.hset(keyName+clientId,str,jsonObject.toString());
        return true;
    }

    @Override
    public Message hreleaseMsg(String clientId, int msgId) {
        Message message = this.hgetMsg(clientId,msgId);
        cluster.hdel(keyName+clientId,String.valueOf(msgId));
        return message;
    }

    @Override
    public <T> Collection<T> hgetAllMsg(String clientId,Class objectClass) {
        ArrayList<T> messagesList = new ArrayList<>();
        if (cluster.exists(keyName+clientId)){
            for (String temp:cluster.hvals(keyName+clientId)){
                messagesList.add((T) JSONObject.toBean(JSONObject.fromObject(temp),objectClass));
            }
           return messagesList;
        }
        return messagesList;
    }

    @Override
    public boolean hcontainMsg(String clientId, int msgId) {
        return cluster.hexists(keyName+clientId,String.valueOf(msgId));
    }

    @Override
    public Collection<Message> sgetAllMsg() {
        Set<Message> messages = new HashSet<>();
        ScanParams scanParams = new ScanParams();
        scanParams.match(keyName+"*");
        scanParams.count(1000);
        String scanRet = "0";
        do {
            ScanResult<String> result = cluster.scan(scanRet, scanParams);
            scanRet = result.getStringCursor();
            result.getResult().forEach(key -> {
                if (cluster.exists(key)){
                    messages.add((Message) JSONObject.toBean(JSONObject.fromObject(cluster.get(key)),Message.class));
                }
            });
        }while (!scanRet.equals("0"));
        return messages;
    }

    @Override
    public void sstoreMsg(String str, Message message) {
        jsonObject = JSONObject.fromObject(message);
        cluster.set(keyName+str,jsonObject.toString());
    }

    @Override
    public boolean scontain(String str) {
        return cluster.exists(keyName+str);
    }

    @Override
    public <T> T sgetSetMsg(String str,Object obj) {
        jsonObject = JSONObject.fromObject(obj);
        String temp = cluster.getSet(keyName+str,jsonObject.toString());
        return (T) JSONObject.toBean(JSONObject.fromObject(temp),obj.getClass());
    }

    @Override
    public <T> T sgetMsg(String str,Class objectClass) {
        jsonObject = JSONObject.fromObject(cluster.get(keyName+str));
        return (T) JSONObject.toBean(jsonObject,objectClass);
    }

    @Override
    public Collection<Message> lgetAllMsg(String str) {
        Collection<Message> allMessage = new ArrayList<>();
        for (String temp:cluster.lrange(keyName+str,0,-1)){
            allMessage.add((Message) JSONObject.toBean(JSONObject.fromObject(temp),Message.class));
        }
        return allMessage;
    }

    @Override
    public boolean laddMsg(Integer num,String str, Message message) {
        if (cluster.exists(keyName+str)){
            if (cluster.lrange(keyName+str,0,-1).size() > num){
                cluster.lpop(keyName+str);
            }
        }
        jsonObject = JSONObject.fromObject(message);
        cluster.rpush(keyName+str,jsonObject.toString());
        return true;
    }


}
