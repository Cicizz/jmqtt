package com.jmqtt.mqtt


import com.jmqtt.mqtt.v3.acceptance.util.MqttAclClientFactory
import com.hivemq.client.mqtt.MqttClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AbstractAclMqttSpecification extends AbstractMqtt5Specification{

    private static final Logger logger = LoggerFactory.getLogger(AbstractAclMqttSpecification.class);

    @Value('${dnc.user}')
    protected String dncUserName;

    void connectAsDnc(MqttClient client){
        MqttAclClientFactory.connectAsDncInstance(client,dncUserName)
        sleep(100)
    }

    void connectTLSAsDnc(MqttClient client){
        MqttAclClientFactory.connectTLSAsDncInstance(client, dncUserName)
        sleep(100)
    }

    void connectAsDevice(MqttClient client){
        MqttAclClientFactory.connectAsDevice(client)
        sleep(100)
    }


}
