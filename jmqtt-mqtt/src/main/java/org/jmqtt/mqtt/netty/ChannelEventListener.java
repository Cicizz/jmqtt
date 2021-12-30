package org.jmqtt.mqtt.netty;

import io.netty.channel.Channel;

public interface ChannelEventListener {

    /**
     * channel connect event
     */
    void onChannelConnect(String remoteAddr, Channel channel);

    /**
     * channel close
     */
    void onChannelClose(String remoteAddr,Channel channel);

    /**
     * channel heartbeat over time
     */
    void onChannelIdle(String remoteAddr,Channel channel);

    /**
     * channel exception
     */
    void onChannelException(String remoteAddr,Channel channel);
}
