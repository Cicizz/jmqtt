package com.jmqtt.mqtt.v3.acceptance.util;

import com.jmqtt.mqtt.v3.acceptance.constant.MqttV1ClientConstant;
import com.jmqtt.mqtt.v3.acceptance.model.MqttServer;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class MqttV1ClientFactory extends MqttClientFactory{


    public MqttV1ClientFactory(MqttServer mqttServer){
        super(mqttServer);
    }

    public Mqtt5Client createMqttV1Device(String dsn, String proof) {
        return Mqtt5Client.builder()
            .serverHost(getMqttServer().getHost())
            .serverPort(getMqttServer().getPort())
            .identifier(dsn)
            .simpleAuth()
            .username(MqttV1ClientConstant.INSTANCE.getProofUserName())
            .password(proof.getBytes())
            .applySimpleAuth()
            .build();
    }

//    public static void subscribe(Mqtt5AsyncClient mqtt5Client){
//        mqtt5Client.
//
//
//    public static String wrapHttpRequestToString(){
//        return "";
//    }


}
