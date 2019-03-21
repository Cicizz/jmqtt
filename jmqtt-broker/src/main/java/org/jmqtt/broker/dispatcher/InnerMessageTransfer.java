package org.jmqtt.broker.dispatcher;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.processor.AbstractMessageProcessor;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
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
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * transfer message in cluster
 */
public class InnerMessageTransfer{
    private static final Logger msgLog = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private static final Logger connLog = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);
    private static final Logger clusterLog = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private MessageDispatcher messageDispatcher;
    private RetainMessageStore retainMessageStore;
    private MessageTransfer messageTransfer;
    private SessionStore sessionStore;
    private SubscriptionMatcher subscriptionMatcher;
    private SubscriptionStore subscriptionStore;
    private FlowMessageStore flowMessageStore;
    private WillMessageStore willMessageStore;
    private OfflineMessageStore offlineMessageStore;


    public InnerMessageTransfer(BrokerController brokerController,MessageTransfer messageTransfer) {
        this.messageDispatcher = brokerController.getMessageDispatcher();
        this.retainMessageStore = brokerController.getRetainMessageStore();
        this.messageTransfer = messageTransfer;
        this.sessionStore = brokerController.getSessionStore();
        this.subscriptionStore = brokerController.getSubscriptionStore();
        this.subscriptionMatcher = brokerController.getSubscriptionMatcher();
        this.flowMessageStore = brokerController.getFlowMessageStore();
        this.willMessageStore = brokerController.getWillMessageStore();
        this.offlineMessageStore = brokerController.getOfflineMessageStore();
    }



    public void init() {
        MessageListener listener = new InnerMessageListener();
        this.messageTransfer.registerListener(listener);
    }

    public void send2AllNodes(ClusterRemotingCommand clusterRemotingCommand) {
        try {
            this.messageTransfer.send(clusterRemotingCommand);
        } catch (Exception ex) {
            msgLog.warn("send message error", ex);
        }
    }

    public void send(String nodeName, ClusterRemotingCommand clusterRemotingCommand) {
        try {
            this.messageTransfer.send(nodeName, clusterRemotingCommand);
        } catch (Exception ex) {
            msgLog.warn("send message error", ex);
        }
    }


    private class InnerMessageListener implements MessageListener {

        private ThreadPoolExecutor clusterMessageExecutor;

        private InnerMessageListener(){
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
                            case ClusterRequestCode.SEND_MESSAGE:
                                dispatcherMessage(clusterRemotingCommand);
                                break;
                            case ClusterRequestCode.TRANSFER_SESSION:
                                loadRemoteSession(clusterRemotingCommand);
                                break;
                            case ClusterRequestCode.TRANSFER_SESSION_MESSAGE:
                                dispatcherSessionMessage(clusterRemotingCommand);
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

    private void dispatcherSessionMessage(ClusterRemotingCommand command){
        byte[] body = command.getBody();
        if(body == null){
            connLog.warn("transfer session message,message body is null");
            return;
        }
        Message message = SerializeHelper.deserialize(body,Message.class);
        if(message == null){
            connLog.warn("transfer session message,message is null");
            return;
        }
        String clientId = message.getClientId();
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        if(clientSession == null){
            connLog.warn("transfer session message,client is null,clientId={}",clientId);
            if(sessionStore.getLastSession(clientId) == null){
                connLog.warn("transfer session message,the client has no session in this node,clientId={}",clientId);
                sessionStore.setSession(clientId,System.currentTimeMillis());
                this.offlineMessageStore.addOfflineMessage(clientId,message);
                return;
            }
        }
        dispatcherMessage2Client(clientSession,message);
    }

    private void dispatcherMessage(ClusterRemotingCommand command){
        msgLog.debug("process dispatcher message to all client that connected on this nod");
        byte[] body = command.getBody();
        if(body == null){
            msgLog.warn("process dispatcher message error,command body is null");
            return;
        }
        Message message = SerializeHelper.deserialize(body,Message.class);
        if(message == null){
            msgLog.warn("process dispatcher message error,message is null");
            return;
        }
        this.messageDispatcher.appendMessage(message);
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if(retain){
            int qos = (int) message.getHeader(MessageHeader.QOS);
            byte[] payload = message.getPayload();
            String topic = (String) message.getHeader(MessageHeader.TOPIC);
            //qos == 0 or payload is none,then clear previous retain message
            if(qos == 0 || payload == null || payload.length == 0){
                this.retainMessageStore.removeRetainMessage(topic);
            }else{
                this.retainMessageStore.storeRetainMessage(topic,message);
            }
        }
    }

    private void dispatcherMessage2Client(ClientSession clientSession,Message message){
        connLog.debug("process dispatcher session message,clientId={}",clientSession.getClientId());
        int qos = (int) message.getHeader(MessageHeader.QOS);
        if(qos > 0){
            flowMessageStore.cacheSendMsg(clientSession.getClientId(),message);
        }
        MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false,qos,clientSession.generateMessageId());
        clientSession.getCtx().writeAndFlush(publishMessage);
    }

    private void loadRemoteSession(ClusterRemotingCommand command){
        byte[] body = command.getBody();
        if(body == null){
            connLog.warn("transfer session command is null");
            return;
        }
        List<Subscription> subscriptions = SerializeHelper.deserializeList(body,Subscription.class);
        if(subscriptions == null){
            connLog.warn("transfer session,load remote session is null");
            return;
        }
        for(Subscription subscription : subscriptions){
            String clientId = subscription.getClientId();

            ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
            if(clientSession == null){
                connLog.warn("cluster transfer session the client has disconnected,clientId={}",clientId);
                Object state = sessionStore.getLastSession(clientId);
                if(state == null){
                    connLog.warn("cluster transfer session this node has no session of this client,clientId={}",clientId);
                    return;
                }
            }
            this.subscriptionMatcher.subscribe(subscription);
            this.subscriptionStore.storeSubscription(clientId,subscription);
        }
    }

    private void newClientOnline(ClusterRemotingCommand clusterRemotingCommand) {
        byte[] body = clusterRemotingCommand.getBody();
        if (body == null) {
            connLog.warn("cluster new client online command is null");
            return;
        }
        ClientSession originSession = SerializeHelper.deserialize(body, ClientSession.class);
        if (originSession == null) {
            connLog.warn("cluster new client online command is null");
            return;
        }
        String clientId = originSession.getClientId();
        String originNode = clusterRemotingCommand.getExtField().get(CommandConstant.NODE_NAME);
        ClientSession currentNodeSession = ConnectManager.getInstance().getClient(clientId);
        if (currentNodeSession != null) {
            connLog.debug("this client is connecting to this node,clientId={}", clientId);
            if (originSession.isCleanSession()) {
                clearSession(clientId);
            } else {
                transferSession(clientId, originNode);
                transferOfflineMessage(clientId,originNode);
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
            if(originSession.isCleanSession()){
                clearSession(clientId);
            }else{
                transferSession(clientId, originNode);
                transferOfflineMessage(clientId,originNode);
            }
            return;
        }
        long offlineTime = Long.parseLong(connState.toString());
        if(originSession.isCleanSession()){
            clearSession(clientId);
        }else{
            transferSession(clientId, originNode);
            transferOfflineMessage(clientId,originNode);
        }
        connLog.debug("the client last offline time,time={}", MixAll.dateFormater(offlineTime));
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
        ClusterRemotingCommand remotingCommand = new ClusterRemotingCommand(ClusterRequestCode.TRANSFER_SESSION);
        remotingCommand.setBody(body);
        send(originNode,remotingCommand);
        this.subscriptionStore.clearSubscription(clientId);
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
    }

    private void transferOfflineMessage(String clientId, String originNode) {
        Collection<Message> flowSendMessages = this.flowMessageStore.getAllSendMsg(clientId);
        for(Message message : flowSendMessages){
            message.setClientId(clientId);
            byte[] body = SerializeHelper.serialize(message);
            ClusterRemotingCommand remotingCommand = new ClusterRemotingCommand(ClusterRequestCode.TRANSFER_SESSION_MESSAGE,body);
            send(originNode,remotingCommand);
        }
        this.flowMessageStore.clearClientFlowCache(clientId);
        Collection<Message> offlineMessages =  this.offlineMessageStore.getAllOfflineMessage(clientId);
        for(Message message : offlineMessages){
            message.setClientId(clientId);
            byte[] body = SerializeHelper.serialize(message);
            ClusterRemotingCommand remotingCommand = new ClusterRemotingCommand(ClusterRequestCode.TRANSFER_SESSION_MESSAGE,body);
            send(originNode,remotingCommand);
        }
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
    }

}
