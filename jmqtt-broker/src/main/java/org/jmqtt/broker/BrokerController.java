package org.jmqtt.broker;

import org.jmqtt.common.config.BrokerConfig;
import org.jmqtt.common.config.NettyConfig;

public class BrokerController {
    private BrokerConfig brokerConfig;
    private NettyConfig nettyConfig;

    public BrokerController(BrokerConfig brokerConfig, NettyConfig nettyConfig){
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;
    }

    public void start(){

    }

    public void shutdown(){

    }
}
