package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.bean.Message;
import org.jmqtt.remoting.netty.RequestProcessor;

public class PingProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, Message message) {

    }
}
