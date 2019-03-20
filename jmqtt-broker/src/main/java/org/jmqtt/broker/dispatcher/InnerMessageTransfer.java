package org.jmqtt.broker.dispatcher;

import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.helper.ThreadFactoryImpl;
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
import org.jmqtt.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private OfflineMessageStore offlineMessageStore;


    public InnerMessageTransfer() { }

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

    public void send(String nodeName, int code, byte[] body) {
        try {
            ClusterRemotingCommand clusterRemotingCommand = new ClusterRemotingCommand(code);
            this.messageTransfer.send(nodeName, clusterRemotingCommand);
        } catch (Exception ex) {
            msgLog.warn("send message error", ex);
        }
    }


    private class InnerMessageListener implements MessageListener {

        private ThreadPoolExecutor clusterMessageExecutor;

        public InnerMessageListener(){
            this.clusterMessageExecutor = new ThreadPoolExecutor(8,
                    8,
                    60000,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(10000),
                    new ThreadFactoryImpl("ClusterMessageExecutor"));
        }

        @Override
        public ReceiveMessageStatus receive(ClusterRemotingCommand clusterRemotingCommand) {
            int code = clusterRemotingCommand.getCode();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
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
                        }
                    } catch (Exception ex) {
                        msgLog.warn("process receive cluster message exception", ex);
                    }
                }
            };
            clusterMessageExecutor.submit(runnable);
            return ReceiveMessageStatus.OK;
        }
    }

    private void newClientOnline(ClusterRemotingCommand clusterRemotingCommand) {
        byte[] body = clusterRemotingCommand.getBody();
        if (body == null) {
            connLog.warn("cluster new client online command is null");
            return;
        }
        ClientSession clientSession = SerializeHelper.deserialize(body, ClientSession.class);
        String clientId = clientSession.getClientId();
        if (clientSession == null) {
            connLog.warn("cluster new client online command is null");
            return;
        }
        String originNode = clusterRemotingCommand.getExtField().get(CommandConstant.NODE_NAME);
        ClientSession currentNodeSession = ConnectManager.getInstance().getClient(clientId);
        if (currentNodeSession != null) {
            connLog.debug("this client is connecting to this node,clientId={}", clientId);
            if (clientSession.isCleanSession()) {
                clearSession(clientId);
            } else {
                transferSession(clientId, originNode);
            }
            willMessageStore.removeWillMessage(clientId);
            currentNodeSession.getCtx().close();
            ConnectManager.getInstance().removeClient(clientId);
            return;
        }
        Object connState = sessionStore.getLastSession(clientId);
        if (connState == null) {
            connLog.debug("the client has't ever connect to this node,clientId={}", clientId);
            return;
        }
        if (connState.equals(true)) {
            connLog.warn("there is a bug of this client in this node,clientId={}", clientId);
            transferSession(clientId, originNode);
            return;
        }
        long offlineTime = Long.parseLong(connState.toString());
        transferSession(clientId, originNode);
        connLog.debug("the client last offline time,time={}", MixAll.dateFormater(offlineTime));
        transferOfflineMessage(clientId,originNode);
    }

    private void clearSession(String clientId) {
        this.sessionStore.clearSession(clientId);
        Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
        for (Subscription subscription : subscriptions) {
            this.subscriptionMatcher.unSubscribe(subscription.getTopic(), clientId);
        }
        this.subscriptionStore.clearSubscription(clientId);
        this.flowMessageStore.clearClientFlowCache(clientId);
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
    }

    private void transferSession(String clientId, String originNode) {
        ServerNode serverNode = ClusterNodeManager.getInstance().getNode(originNode);
        if (serverNode == null || !serverNode.isActive()) {
            connLog.warn("new connect origin node is not active,nodeName={}", originNode);
            Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
            for (Subscription subscription : subscriptions) {
                this.subscriptionMatcher.unSubscribe(subscription.getTopic(), clientId);
            }
            return;
        }
        this.sessionStore.clearSession(clientId);
        Collection<Subscription> subscriptions = this.subscriptionStore.getSubscriptions(clientId);
        for (Subscription subscription : subscriptions) {
            this.subscriptionMatcher.unSubscribe(subscription.getTopic(), clientId);
        }
        byte[] body = SerializeHelper.serialize(subscriptions);
        send(originNode, ClusterRequestCode.TRANSFER_SESSION, body);
        this.subscriptionStore.clearSubscription(clientId);
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
    }

    private void transferOfflineMessage(String clientId, String originNode) {
        Collection<Message> flowSendMessages = this.flowMessageStore.getAllSendMsg(clientId);
        for(Message message : flowSendMessages){
            byte[] body = SerializeHelper.serialize(message);
            send(originNode,ClusterRequestCode.TRANSFER_OFFLINE_MESSAGE,body);
        }
        this.flowMessageStore.clearClientFlowCache(clientId);
        Collection<Message> offlineMessages =  this.offlineMessageStore.getAllOfflineMessage(clientId);
        for(Message message : offlineMessages){
            byte[] body = SerializeHelper.serialize(message);
            send(originNode,ClusterRequestCode.TRANSFER_OFFLINE_MESSAGE,body);
        }
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
    }

}
