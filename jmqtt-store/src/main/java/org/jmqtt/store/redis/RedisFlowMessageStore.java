
package org.jmqtt.store.redis;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RedisFlowMessageStore implements FlowMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RedisTemplate redisTemplate;

    private final String SEND_FLOW_MESSAGE = RedisTemplate.PROJECT + "_SEND_FLOW_";
    private final String REC_FLOW_MESSAGE = RedisTemplate.PROJECT + "_REC_FLOW_";

    public RedisFlowMessageStore(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void clearClientFlowCache(String clientId) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                jedis.del(SEND_FLOW_MESSAGE + clientId);
                jedis.del(REC_FLOW_MESSAGE + clientId);
                return null;
            };
        });
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        Object msgStr = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hget(REC_FLOW_MESSAGE+clientId,String.valueOf(msgId));
            }
        });
        if (msgStr == null) {
            log.warn("[Redis] getRecMsg error,message is null,clientId:{},msgId:{}",clientId,msgId);
            return null;
        };
        return JSONObject.parseObject(msgStr.toString(),Message.class);
    }


    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hset(REC_FLOW_MESSAGE + clientId,String.valueOf(message.getMsgId()),JSONObject.toJSONString(message));
            }
        });
        return true;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        Object msgObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                String msgStr = jedis.hget(REC_FLOW_MESSAGE + clientId,String.valueOf(msgId));
                jedis.hdel(REC_FLOW_MESSAGE + clientId,String.valueOf(msgId));
                return msgStr;
            }
        });
        if (msgObj == null) {
            log.warn("[Redis] releaseRecMsg error,message is null,clientId:{},msgId:{}",clientId,msgId);
            return null;
        };
        return JSONObject.parseObject(msgObj.toString(),Message.class);
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hset(SEND_FLOW_MESSAGE + clientId,String.valueOf(message.getMsgId()),JSONObject.toJSONString(message));
            }
        });
        return true;
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        Object msgObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hgetAll(SEND_FLOW_MESSAGE+clientId);
            }
        });
        Collection<Message> messages = new ArrayList<>();
        if (msgObj != null) {
            Map<String,String> map = (Map<String, String>) msgObj;
            map.values().forEach(item -> {
                ((ArrayList<Message>) messages).add(JSONObject.parseObject(item,Message.class));
            });
        }
        return messages;
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hdel(SEND_FLOW_MESSAGE + clientId,String.valueOf(msgId));
            };
        });
        return true;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        Object existsObj = redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.hexists(SEND_FLOW_MESSAGE+clientId,String.valueOf(msgId));
            }
        });
        return BooleanUtils.toBoolean(existsObj.toString());
    }
}