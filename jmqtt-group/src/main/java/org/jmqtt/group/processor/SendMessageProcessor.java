package org.jmqtt.group.processor;

import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.message.ReceiveMessageStatus;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageProcessor implements ClusterRequestProcessor{

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private MessageListener messageListener;

    public SendMessageProcessor(MessageListener messageListener){
        this.messageListener = messageListener;
    }

    @Override
    public ClusterRemotingCommand processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        ReceiveMessageStatus result = this.messageListener.receive(cmd);
        ClusterRemotingCommand response = null;
        if(result == ReceiveMessageStatus.OK){
            response = new ClusterRemotingCommand(ClusterResponseCode.RESPONSE_OK);
        } else {
            response = new ClusterRemotingCommand(ClusterResponseCode.ERROR_RESPONSE);
        }
        return response;
    }
}
