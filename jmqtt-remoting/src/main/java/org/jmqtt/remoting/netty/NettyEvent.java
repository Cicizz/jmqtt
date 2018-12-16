package org.jmqtt.remoting.netty;

import io.netty.channel.Channel;

public class NettyEvent {

    private String remoteAddr;
    private NettyEventType eventType;
    private Channel channel;

    public NettyEvent(String remoteAddr, NettyEventType eventType, Channel channel) {
        this.remoteAddr = remoteAddr;
        this.eventType = eventType;
        this.channel = channel;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public NettyEventType getEventType() {
        return eventType;
    }

    public Channel getChannel() {
        return channel;
    }
}
