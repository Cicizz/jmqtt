
package org.jmqtt.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import org.jmqtt.bus.BusController;
import org.jmqtt.mqtt.event.MqttEventListener;
import org.jmqtt.mqtt.netty.MqttRemotingServer;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.config.NettyConfig;
import org.jmqtt.support.helper.MixAll;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * mqtt服务
 */
public class MQTTServer {

    private static final Logger log = JmqttLogger.mqttLog;

    private BusController busController;
    private BrokerConfig brokerConfig;
    private NettyConfig nettyConfig;
    private MqttRemotingServer mqttRemotingServer;
    private MqttEventListener mqttEventListener;

    public MQTTServer(BusController busController, BrokerConfig brokerConfig, NettyConfig nettyConfig){
        this.busController = busController;
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;
        this.mqttRemotingServer = new MqttRemotingServer(brokerConfig,nettyConfig,busController);
        this.mqttEventListener = new MqttEventListener(busController.getDeviceMessageManager());
    }

    public void start(){
        this.busController.getClusterEventManager().registerEventListener(mqttEventListener);
        this.mqttRemotingServer.start();
        LogUtil.info(log,"MQTT server start success.");
    }


    public void shutdown(){
        this.mqttRemotingServer.shutdown();
        Collection<MQTTConnection> mqttConnections = ConnectManager.getInstance().getAllConnections();
        if (!MixAll.isEmpty(mqttConnections)) {
            for (MQTTConnection mqttConnection : mqttConnections) {
                mqttConnection.abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
            }
        }
    }
}
