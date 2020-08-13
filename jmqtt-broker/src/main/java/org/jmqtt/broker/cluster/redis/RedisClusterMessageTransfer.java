
package org.jmqtt.broker.cluster.redis;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.cluster.ClusterMessageTransfer;
import org.jmqtt.broker.cluster.command.CommandCode;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.store.redis.RedisCallBack;
import org.jmqtt.store.redis.RedisMqttStore;
import org.jmqtt.store.redis.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisClusterMessageTransfer extends ClusterMessageTransfer {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private RedisTemplate redisTemplate;

    private static final String T_REDIS = "T_JMQTT_REDIS";

    private ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryImpl("REDIS_CLUSTER_CONSUME_THREAD"));

    public RedisClusterMessageTransfer(MessageDispatcher messageDispatcher, RedisMqttStore redisMqttStore) {
        super(messageDispatcher);
        this.redisTemplate = redisMqttStore.getRedisTemplate();
    }

    @Override
    public CommandReqOrResp sendMessage(CommandReqOrResp commandReqOrResp) {
        this.redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                return jedis.publish(T_REDIS, JSONObject.toJSONString(commandReqOrResp.getBody()));
            }
        });
        CommandReqOrResp response = new CommandReqOrResp(commandReqOrResp.getCommandCode());
        return response;
    }

    private void subscribe(){
        this.redisTemplate.operate(new RedisCallBack() {
            @Override
            public Object operate(Jedis jedis) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        log.debug("[Cluster] rec message from redis channel:{}",channel);
                        CommandReqOrResp request = new CommandReqOrResp(CommandCode.MESSAGE_CLUSTER_TRANSFER,JSONObject.parseObject(message,
                                Message.class));
                        consumeClusterMessage(request);
                    }
                },T_REDIS);
                return null;
            }
        });
    }

    @Override
    public void startup() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                subscribe();
            }
        });
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}