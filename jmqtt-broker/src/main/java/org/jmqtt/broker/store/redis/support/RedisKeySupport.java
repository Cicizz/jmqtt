package org.jmqtt.broker.store.redis.support;

public interface RedisKeySupport {

    String PREFIX = "JMQTT";
    String SEND_FLOW_MESSAGE = PREFIX + "_SEND_FLOW_";
    String SEND_FLOW_SEC_MESSAGE = PREFIX + "_SEND_FLOW_SEC_";
    String REC_FLOW_MESSAGE = PREFIX + "_REC_FLOW_";
    String OFFLINE = PREFIX + "_OFFLINE_";
    String RETAIN = PREFIX + "_RETAIN";
    String SESSION = PREFIX + "_SESSION_";
    String SUBSCRIPTION = PREFIX + "_SUBSCRIPTION_";
    String WILL = PREFIX + "_WILL_";
}
