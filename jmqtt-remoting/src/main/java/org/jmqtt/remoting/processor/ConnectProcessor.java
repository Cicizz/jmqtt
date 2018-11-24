package org.jmqtt.remoting.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.config.BrokerConfig;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ConnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER);

    private BrokerConfig brokerConfig;

    public ConnectProcessor(BrokerConfig brokerConfig){
        this.brokerConfig = brokerConfig;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, Message message) {
        MqttConnectMessage connectMessage = (MqttConnectMessage) message.getPayload();
        MqttConnectReturnCode returnCode = null;
        boolean sessionPresent = false;
        int mqttVersion = connectMessage.variableHeader().version();
        String clientId = connectMessage.payload().clientIdentifier();
        boolean cleansession = connectMessage.variableHeader().isCleanSession();
        String userName = connectMessage.payload().userName();
        byte[] password = connectMessage.payload().passwordInBytes();
        if(!versionValid(mqttVersion)){
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
        }
        if(!clientIdVerfy(clientId)){
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
        }
        if(onBlackList(clientId)){
           returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
        }
        if(!authentication(clientId,userName,password)){
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
        }
        ClientSession clientSession = loadClientSession(clientId,cleansession);
        clientSession.setCtx(ctx);
        boolean willFlag = connectMessage.variableHeader().isWillFlag();



    }

    private void storeWillMsg(){

    }

    /**
     * if cleansession is true, load previous session and republish messages
     * else create new ClientSession
     */
    private ClientSession loadClientSession(String clientId,boolean cleanSession){
        ClientSession clientSession = new ClientSession(clientId,cleanSession);
        if(cleanSession){
            //TODO  1.clean subscriptions  2.clean messages
        }else{
            //TODO 1.load subscriptions;
            List<Subscription> subscriptionList = new ArrayList<>();
            clientSession.setSubscriptions(subscriptionList);
            //TODO 2.republish messages;
            if(subscriptionList != null){
                for(Subscription subscription : subscriptionList){
                    //pub messages
                }
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
