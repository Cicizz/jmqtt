package org.jmqtt.remoting.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.remoting.netty.Message;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     */
    void processRequest(ChannelHandlerContext ctx,Message message);
}
