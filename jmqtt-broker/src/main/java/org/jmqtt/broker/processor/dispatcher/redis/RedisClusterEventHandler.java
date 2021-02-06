package org.jmqtt.broker.processor.dispatcher.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.store.redis.support.RedisKeySupport;
import org.jmqtt.broker.store.redis.support.RedisSupport;
import org.jmqtt.broker.store.redis.support.RedisUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.UUID;

public class RedisClusterEventHandler implements ClusterEventHandler {
    private static final String INSTANCE_CHANNEL_ID = RedisKeySupport.PREFIX + "_CLUSTER_CHANNEL_" + UUID.randomUUID();
    private static final String INSTANCE_CHANNEL_PATTERN = RedisKeySupport.PREFIX + "_CLUSTER_CHANNEL_*";
    private EventConsumeHandler eventConsumeHandler;
    private RedisSupport redisSupport;

    @Override
    public void start(BrokerConfig brokerConfig) {
        this.redisSupport = RedisUtils.getInstance().createSupport(brokerConfig);
        this.redisSupport.operate(jedis -> {
            jedis.psubscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    eventConsumeHandler.consumeEvent(JSONObject.parseObject(message,Event.class));
                }
            }, INSTANCE_CHANNEL_PATTERN);
            return true;
        });
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean sendEvent(Event event) {
        redisSupport.operate(jedis -> jedis.publish(INSTANCE_CHANNEL_ID,JSONObject.toJSONString(event)));
        return true;
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {
        this.eventConsumeHandler = eventConsumeHandler;
    }

    @Override
    public List<Event> pollEvent(int maxPollNum) {
        return null;
    }
}
