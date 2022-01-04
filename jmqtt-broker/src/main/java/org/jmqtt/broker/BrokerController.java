package org.jmqtt.broker;

import org.jmqtt.bus.BusController;
import org.jmqtt.mqtt.MQTTServer;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.config.NettyConfig;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

/**
 * 具体控制类：负责加载配置文件，初始化环境，启动服务等
 */
public class BrokerController {

    private static final Logger log = JmqttLogger.brokerLog;

    private BrokerConfig brokerConfig;
    private NettyConfig nettyConfig;

    /** bus */
    private BusController busController;

    /** mqtt gateway */
    private MQTTServer mqttServer;

    /** coap gateway todo */

    public BrokerController(BrokerConfig brokerConfig, NettyConfig nettyConfig){
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;
        this.busController = new BusController(brokerConfig);
        this.mqttServer = new MQTTServer(busController,brokerConfig,nettyConfig);
    }

    public void start(){
        this.busController.start();
        this.mqttServer.start();
        LogUtil.info(log,"Jmqtt start. version:" + brokerConfig.getVersion());
    }


    public void shutdown(){
        this.mqttServer.shutdown();
        this.busController.shutdown();
    }
}
