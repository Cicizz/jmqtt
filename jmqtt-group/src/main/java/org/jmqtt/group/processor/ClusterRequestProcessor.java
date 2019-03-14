package org.jmqtt.group.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.group.protocol.ClusterRemotingCommand;

public interface ClusterRequestProcessor {

    void processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd);
}
