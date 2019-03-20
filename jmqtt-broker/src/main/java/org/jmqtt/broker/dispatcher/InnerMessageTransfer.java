package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.MessageTransfer;
import org.jmqtt.group.common.ClusterNodeManager;
import org.jmqtt.group.message.MessageListener;
import org.jmqtt.group.message.ReceiveMessageStatus;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.jmqtt.group.protocol.CommandConstant;
import org.jmqtt.group.protocol.node.ServerNode;
import org.jmqtt.remoting.session.ClientSession;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.SessionStore;
import org.jmqtt.store.SubscriptionStore;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * transfer message in cluster
 */
public class InnerMessageTransfer {
    private static final Logger msgLog = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private static final Logger connLog = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);
    private static final Logger clusterLog = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private MessageTransfer messageTransfer;
    private SessionStore sessionStore;
    private SubscriptionMatcher subscriptionMatcher;
    private SubscriptionStore subscriptionStore;
    private FlowMessageStore flowMessageStore;
    private WillMessageStore willMessageStore;


    public InnerMessageTransfer() {
    }

    public InnerMessageTransfer(MessageTransfer messageTransfer) {
        this.messageTransfer = messageTransfer;
    }

    public void registerMessageTransfer(MessageTransfer messageTransfer) {
        this.messageTransfer = messageTransfer;
    }

    public void init() {
        MessageListener listener = new InnerMessageListener();
        this.messageTransfer.registerListener(listener);
    }

    public void send(int code, byte[] body) {
        try {
            ClusterRemotingCommand clusterRemotingCommand = new ClusterRemotingCommand(code);
            this.messageTransfer.send(clusterRemotingCommand);
        } catch (Exception ex) {
            msgLog.warn("send message error", ex);
        }
    }

    public void send(String nodeName,int code, byte[] body) {
        try {
            ClusterRemotingCommand clusterRemotingCommand = new ClusterRemotingCommand(code);
            this.messageTransfer.send(nodeName,clusterRemotingCommand);
        } catch (Exception ex) {
            msgLog.warn("send message error", ex);
        }
    }


    private class InnerMessageListener implements MessageListener {

        @Override
        public ReceiveMessageStatus receive(ClusterRemotingCommand clusterRemotingCommand) {
            int code = clusterRemotingCommand.getCode();
            try{
                switch (code) {
                    case ClusterRequestCode
                            .NOTICE_NEW_CLIENT:
                        newClientOnline(clusterRemotingCommand);
                        break;
                    case ClusterRequestCode.NOTICE_NEW_SUBSCRIPTION:
                        // TODO
                        break;
                    case ClusterRequestCode.SEND_MESSAGE:
                        // TODO
                        break;
                    case ClusterRequestCode.TRANSFER_SESSION:

                        break;
                    case ClusterRequestCode.TRANSFER_OFFLINE_MESSAGE:

                        break;
                    default:
                        msgLog.info("not supported command,code={}", code);
                        return ReceiveMessageStatus.FAIL;
                }
            }catch (Exception ex){
                msgLog.warn("process receive cluster message exception",ex);
            }
            return ReceiveMessageStatus.OK;
        }
    }

    private void newClientOnline(ClusterRemotingCommand clusterRemotingCommand){
        byte[] body = clusterRemotingCommand.getBody();
        if(body == null){
            connLog.warn("cluster new client online command is null");
            return;
        }
        ClientSession clientSession = SerializeHelper.deserialize(body,ClientSession.class);
        String clientId = clientSession.getClientId();
        if(clientSession == null){
            connLog.warn("cluster new client online command is null");
            return;
        }
        String originNode = clusterRemotingCommand.getExtField().get(CommandConstant.NODE_NAME);
        ClientSession currentNodeSession = ConnectManager.getInstance().getClient(clientId);
        if(currentNodeSession != null){
            connLog.debug("this client is connecting to this node,clientId={}",clientId);
            if(clientSession.isCleanSession()){
                clearSession(clientId);
            }else{
                transferSession(clientId,originNode);
            }
            willMessageStore.removeWillMessage(clientId);
            currentNodeSession.getCtx().close();
            ConnectManager.getInstance().removeClient(clientId);
            return;
        }
        Object connState = sessionStore.getLastSession(clientId);
        // TODO
    }

    private void clearSession(String clientId){
        this.sessionStore.clearSession(clientId);
        Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
        for(Subscription subscription : subscriptions){
            this.subscriptionMatcher.unSubscribe(subscription.getTopic(),clientId);
        }
        this.subscriptionStore.clearSubscription(clientId);
    }

    private void transferSession(String clientId,String originNode){
        ServerNode serverNode = ClusterNodeManager.getInstance().getNode(originNode);
        if(serverNode == null || !serverNode.isActive()){
            connLog.warn("new connect origin node is not active,nodeName={}",originNode);
            Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
            for(Subscription subscription : subscriptions){
                this.subscriptionMatcher.unSubscribe(subscription.getTopic(),clientId);
            }
            return;
        }
        this.sessionStore.clearSession(clientId);
        Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
        for(Subscription subscription : subscriptions){
            this.subscriptionMatcher.unSubscribe(subscription.getTopic(),clientId);
        }
        byte[] body = SerializeHelper.serialize(subscriptions);
        send(originNode,ClusterRequestCode.TRANSFER_SESSION,body);
        this.subscriptionStore.clearSubscription(clientId);
    }

}
