package org.jmqtt.remoting.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class NettyUtil {

    private static final String CLIENT_ID = "clientId";

    private static final AttributeKey<String> clientIdAttr = AttributeKey.valueOf(CLIENT_ID);

    public static final void setClientId(Channel channel,String clientId){
        channel.attr(clientIdAttr).set(clientId);
    }

    public static final String getClientId(Channel channel){
        return channel.attr(clientIdAttr).get();
    }
}
