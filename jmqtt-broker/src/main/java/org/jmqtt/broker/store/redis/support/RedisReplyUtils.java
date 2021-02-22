package org.jmqtt.broker.store.redis.support;

public class RedisReplyUtils {
    public static Boolean isOk(String reply){
        return reply !=null && reply.startsWith("OK");
    }
}
