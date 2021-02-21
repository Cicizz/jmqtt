package com.jmqtt.mqtt.v3.acceptance.model;

import lombok.Data;

@Data
public class MqttServer {

    private String host;

    private Integer port;

    private boolean enableSSL;

}
