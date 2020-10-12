package org.jmqtt.broker.cluster;

import org.jmqtt.broker.cluster.command.CommandCode;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterMessageTransfer implements ClusterHandler {

    protected static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private MessageDispatcher messageDispatcher;

    public ClusterMessageTransfer(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    protected boolean consumeClusterMessage(CommandReqOrResp commandReqOrResp) {
        String commandCode = commandReqOrResp.getCommandCode();
        switch (commandCode) {
            case CommandCode.MESSAGE_CLUSTER_TRANSFER:
                return dispatcheMessage(commandReqOrResp);
            default:
                log.error("Dispatch cluster message error,commandRequest:{}", commandReqOrResp);
        }
        return false;
    }

    private boolean dispatcheMessage(CommandReqOrResp reqOrResp) {
        Object msgObj = reqOrResp.getBody();
        try {
            Message message = (Message) msgObj;
            return messageDispatcher.appendMessage(message);
        } catch (Exception ex) {
            log.error("Consume cluster message error,ex:{}", ex.getMessage());
        }
        return false;
    }

    public abstract CommandReqOrResp sendMessage(CommandReqOrResp commandReqOrResp);
}