package org.jmqtt.broker.dispatcher;

import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.MessageTransfer;
import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.message.ReceiveMessageStatus;
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

    public void init(){
        MessageListener listener = new InnerMessageListener();
        this.messageTransfer.registerListener(listener);
    }

    public void send(int code,byte[] body){
        try{
            ClusterRemotingCommand clusterRemotingCommand = new ClusterRemotingCommand(code);
            this.messageTransfer.send(clusterRemotingCommand);
        }catch (Exception ex){
            log.warn("send message error",ex);
        }
    }


    private class InnerMessageListener implements MessageListener{

        @Override
        public ReceiveMessageStatus receive(ClusterRemotingCommand clusterRemotingCommand) {
            int code = clusterRemotingCommand.getCode();
            switch (code){
                case ClusterRequestCode
                        .NOTIC_NEW_CLIENT:
                    // TODO
                    break;
                case ClusterRequestCode.NOTICE_NEW_SUBSCRIPTION:
                    // TODO
                    break;
                case ClusterRequestCode.SEND_MESSAGE:
                    // TODO
                    break;
                default:
                    log.info("not supported command,code={}",code);
                    return ReceiveMessageStatus.FAIL;
            }
            return ReceiveMessageStatus.OK;
        }
    }
}
