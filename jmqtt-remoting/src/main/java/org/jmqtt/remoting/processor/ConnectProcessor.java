package org.jmqtt.remoting.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import org.jmqtt.common.config.BrokerConfig;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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



    }

    private boolean versionValid(int mqttVersion){
        if(mqttVersion == 3 || mqttVersion == 4){
            return true;
        }
        return false;
    }
}
