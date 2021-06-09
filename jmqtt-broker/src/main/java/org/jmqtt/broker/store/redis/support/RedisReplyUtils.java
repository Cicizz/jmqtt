package org.jmqtt.broker.store.redis.support;

import org.apache.commons.lang3.math.NumberUtils;

public class RedisReplyUtils {
    public static Boolean isOk(Object reply){
        if (reply instanceof String) {
            String replyStr = (String) reply;
            return replyStr !=null && replyStr.startsWith("OK");
        } else if (NumberUtils.isDigits(String.valueOf(reply))) {
            return Long.parseLong(String.valueOf(reply)) > 0;
        }
        return false;
    }
}
