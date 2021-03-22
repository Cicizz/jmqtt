package com.jmqtt.mqtt.v3.acceptance.util;

import com.jmqtt.mqtt.v3.acceptance.model.MqttServer;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import java.util.UUID;


public class MqttClientFactory {

    protected MqttServer mqttServer;

    public MqttClientFactory(MqttServer mqttServer){
        this.mqttServer = mqttServer;
    }


    public MqttServer getMqttServer() {
        return mqttServer;
    }

    /***
     * clientId = UUID
     * @return
     */
    public Mqtt5Client createMqtt5Client() {
        return getBasicMqtt5ClientBuilder()
                .build();
    }


    /***
     * create with username, password
     * @param userName
     * @param password
     * @return
     */
    public Mqtt5Client createMqtt5Client(String userName, String password) {
        return getBasicMqtt5ClientBuilder()
                .simpleAuth()
                    .username(userName)
                    .password(password.getBytes())
                .applySimpleAuth()
                .build();

    }

    protected Mqtt5ClientBuilder getBasicMqtt5ClientBuilder() {
        return Mqtt5Client.builder()
            .serverHost(mqttServer.getHost())
            .serverPort(mqttServer.getPort())
            .identifier(getUUID());
    }



    /***
     * Specify clientId
     * @param id
     * @return
     */
    public Mqtt5Client createMqtt5Client(String id) {
        return getBasicMqtt5ClientBuilder()
                .identifier(id)
                .build();

    }

    public Mqtt3Client createMqtt3Client() {
        return getBasicMqtt3ClientBuilder()
        .build();
    }

    public Mqtt3Client createMqtt3Client(String id) {
        return getBasicMqtt3ClientBuilder()
            .identifier(id)
            .build();
    }

    protected Mqtt3ClientBuilder getBasicMqtt3ClientBuilder() {
        return Mqtt3Client.builder()
            .serverHost(mqttServer.getHost())
            .serverPort(mqttServer.getPort())
            .identifier(getUUID());
    }


    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public void setMqttServer(MqttServer mqttServer) {
        this.mqttServer = mqttServer;
    }
}
