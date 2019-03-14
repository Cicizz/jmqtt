package org.jmqtt.group.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRebalanceProcessor implements ClusterRequestProcessor{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    @Override
    public void processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {

    }
}
