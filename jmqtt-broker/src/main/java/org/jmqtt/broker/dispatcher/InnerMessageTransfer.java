package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.ClusterRemotingClient;
import org.jmqtt.group.MessageTransfer;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * transfer message in cluster
 */
public class InnerMessageTransfer {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    private MessageTransfer messageTransfer;

    public InnerMessageTransfer(){}

    public InnerMessageTransfer(MessageTransfer messageTransfer){
        this.messageTransfer = messageTransfer;
    }

    public void registerMessageTransfer(MessageTransfer messageTransfer){
        this.messageTransfer = messageTransfer;
    }

    public void send(int code,byte[] body){
        ClusterRemotingCommand clusterRemotingCommand = new ClusterRemotingCommand(code);
        this.messageTransfer.send(clusterRemotingCommand);
    }

    

}
