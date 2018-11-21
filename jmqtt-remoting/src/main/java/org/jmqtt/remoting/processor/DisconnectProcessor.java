package org.jmqtt.remoting.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.remoting.processor.RequestProcessor;
import org.jmqtt.remoting.netty.Message;

public class DisconnectProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, Message message) {

    }
}
