package org.jmqtt.remoting.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.remoting.netty.Message;

public class PingProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, Message message) {

    }
}
