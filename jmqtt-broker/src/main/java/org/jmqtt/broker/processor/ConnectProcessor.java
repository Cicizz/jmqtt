package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.config.BrokerConfig;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ConnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);

    private BrokerConfig brokerConfig;
    private FlowMessageStore flowMessageStore;
    private WillMessageStore willMessageStore;
    private OfflineMessageStore offlineMessageStore;
    private SubscriptionStore subscriptionStore;
    private SessionStore sessionStore;

    public ConnectProcessor(BrokerController brokerController){
        this.brokerConfig = brokerController.getBrokerConfig();
        this.flowMessageStore = brokerController.getFlowMessageStore();
        this.willMessageStore = brokerController.getWillMessageStore();
        this.offlineMessageStore = brokerController.getOfflineMessageStore();
        this.subscriptionStore = brokerController.getSubscriptionStore();
        this.sessionStore = brokerController.getSessionStore();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttConnectMessage connectMessage = (MqttConnectMessage)mqttMessage;
        MqttConnectReturnCode returnCode = null;
        int mqttVersion = connectMessage.variableHeader().version();
        String clientId = connectMessage.payload().clientIdentifier();
        boolean cleansession = connectMessage.variableHeader().isCleanSession();
        String userName = connectMessage.payload().userName();
        byte[] password = connectMessage.payload().passwordInBytes();
        ClientSession clientSession = null;
        boolean sessionPresent = false;
        try{
            if(!versionValid(mqttVersion)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
            } else if(!clientIdVerfy(clientId)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            } else if(onBlackList(clientId)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
            } else if(!authentication(clientId,userName,password)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
            } else{
                Object lastState = sessionStore.getLastSession(clientId);
                if(Objects.nonNull(lastState) && lastState.equals(true)){
                    //TODO cluster clear and disconnect previous connect
                    ClientSession previousClient = ConnectManager.getInstance().getClient(clientId);
                    if(previousClient != null){
                        previousClient.getCtx().close();
                        ConnectManager.getInstance().removeClient(clientId);
                    }
                }
                if(cleansession){
                    clientSession = createNewClientSession(clientId,ctx);
                    sessionPresent = false;
                }else{
                    if(Objects.nonNull(lastState)){
                        clientSession = reloadClientSession(ctx,clientId);
                        sessionPresent = true;
                    }else{
                        clientSession = new ClientSession(clientId,false);
                        sessionPresent = false;
                    }
                }
                sessionStore.setSession(clientId,true);
                boolean willFlag = connectMessage.variableHeader().isWillFlag();
                if(willFlag){
                    boolean willRetain = connectMessage.variableHeader().isWillRetain();
                    int willQos = connectMessage.variableHeader().willQos();
                    String willTopic = connectMessage.payload().willTopic();
                    byte[] willPayload = connectMessage.payload().willMessageInBytes();
                    storeWillMsg(clientSession,willRetain,willQos,willTopic,willPayload);
                }
                int heartbeatSec = connectMessage.variableHeader().keepAliveTimeSeconds();
                keepAlive(clientSession,heartbeatSec);
                returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
                NettyUtil.setClientId(ctx.channel(),clientId);
                ConnectManager.getInstance().putClient(clientId,clientSession);
            }
            MqttConnAckMessage ackMessage = MessageUtil.getConnectAckMessage(returnCode,sessionPresent);
            ctx.writeAndFlush(ackMessage);
            log.info("[CONNECT] -> {} connect to this mqtt server",clientId);
        }catch(Exception ex){
            log.error("[CONNECT] -> Service Unavailable: cause={}",ex);
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
            MqttConnAckMessage ackMessage = MessageUtil.getConnectAckMessage(returnCode,sessionPresent);
            ctx.writeAndFlush(ackMessage);
        }
    }

    private void keepAlive(ClientSession clientSession,int heatbeatSec){
        //TODO set client keepAlive
    }

    private void storeWillMsg(ClientSession clientSession,boolean willRetain,int willQos,String willTopic,byte[] willPayload){
        Map<String,Object> headers = new HashMap<>();
        headers.put(MessageHeader.RETAIN,willRetain);
        headers.put(MessageHeader.QOS,willQos);
        headers.put(MessageHeader.TOPIC,willTopic);
        headers.put(MessageHeader.WILL,true);
        Message message = new Message(Message.Type.WILL,headers,willPayload);
        message.setClientSession(clientSession);
        willMessageStore.storeWillMessage(clientSession.getClientId(),message);
        log.info("[WillMessageStore] : {} store will message:{}",clientSession.getClientId(),message);
    }

    private ClientSession createNewClientSession(String clientId,ChannelHandlerContext ctx){
        ClientSession clientSession = new ClientSession(clientId,true);
        clientSession.setCtx(ctx);
        //clear previous sessions
        this.flowMessageStore.clearClientFlowCache(clientId);
        this.offlineMessageStore.clearOfflineMsgCache(clientId);
        this.subscriptionStore.clearSubscription(clientId);
        this.sessionStore.clearSession(clientId);
        return clientSession;
    }

    /**
     * cleansession is true, reload client session
     */
    private ClientSession reloadClientSession(ChannelHandlerContext ctx,String clientId){
            ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
            clientSession.setCtx(ctx);
            Collection<Message> flowMsgs = flowMessageStore.getAllSendMsg(clientId);
            for(Message message : flowMsgs){
                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false, (Integer) message.getHeader(MessageHeader.QOS),message.getMsgId());
                clientSession.getCtx().writeAndFlush(publishMessage);
            }
            if(offlineMessageStore.containOfflineMsg(clientId)){
                Collection<Message> messages = offlineMessageStore.getAllOfflineMessage(clientId);
                for(Message message : messages){
                    MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false, (Integer) message.getHeader(MessageHeader.QOS),clientSession.generateMessageId());
                    clientSession.getCtx().writeAndFlush(publishMessage);
                }
            }
            return clientSession;
    }

    private boolean authentication(String clientId,String username,byte[] password){
        return true;
    }

    private boolean onBlackList(String clientId){
        return false;
    }

    private boolean clientIdVerfy(String clientId){
        if(StringUtils.isEmpty(clientId)){
            return false;
        }
        return true;
    }

    private boolean versionValid(int mqttVersion){
        if(mqttVersion == 3 || mqttVersion == 4){
            return true;
        }
        return false;
    }
}
