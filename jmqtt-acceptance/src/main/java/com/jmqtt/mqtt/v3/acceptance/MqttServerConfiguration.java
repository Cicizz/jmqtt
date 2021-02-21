package com.jmqtt.mqtt.v3.acceptance;

import com.jmqtt.mqtt.v3.acceptance.model.MqttServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttServerConfiguration {


    @Bean("basicMqttServer")
    @ConfigurationProperties(prefix="mqtt-broker.basic")
    public MqttServer basicMqttServer(){
        return new MqttServer();
    }

    @Bean("aclMqttServer")
    @ConfigurationProperties(prefix="mqtt-broker.acl")
    public MqttServer aclMqttServer(){
        return new MqttServer();
    }

    @Bean("testMqttServer")
    @ConfigurationProperties(prefix="mqtt-broker.test")
    public MqttServer testMqttServer(){
        return new MqttServer();
    }

}
