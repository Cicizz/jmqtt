package org.jmqtt.remoting.netty;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.bean.Message;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     */
    void processRequest(ChannelHandlerContext ctx,Message message);
}
