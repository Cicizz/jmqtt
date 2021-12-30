
package org.jmqtt.mqtt;

import org.jmqtt.bus.BusController;
import org.jmqtt.mqtt.netty.MqttRemotingServer;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.config.NettyConfig;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

/**
 * mqtt服务
 */
public class MQTTServer {

    private static final Logger log = JmqttLogger.mqttLog;

    private BusController busController;
    private BrokerConfig brokerConfig;
    private NettyConfig nettyConfig;
    private MqttRemotingServer mqttRemotingServer;

    public MQTTServer(BusController busController, BrokerConfig brokerConfig, NettyConfig nettyConfig){
        this.busController = busController;
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;
        this.mqttRemotingServer = new MqttRemotingServer(brokerConfig,nettyConfig,busController);
    }

    public void start(){
        this.mqttRemotingServer.start();
        LogUtil.info(log,"MQTT server start success.");
    }


    public void shutdown(){
        this.mqttRemotingServer.shutdown();
    }
}
