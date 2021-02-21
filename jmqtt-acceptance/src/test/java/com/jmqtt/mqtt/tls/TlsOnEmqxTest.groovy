package com.jmqtt.mqtt.tls


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttAclClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.util.concurrent.atomic.AtomicInteger

class TlsOnEmqxTest extends AbstractAclMqttSpecification{

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttAclClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttAclClientFactory(aclServer)
    }

    def "scenario 1:ignore verify certificate"(){
        given:
            //general info
            def message = "upstream_happy_path"
        and:
            //device Info
            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "TESTDSN_619357_000"
            def topic = "UP/" + deviceDSN
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()

            //dnc Info
            def dnc = aclClientFactory.createTLSMqtt5Client().toAsync()
            def dncSubscribeQoS = MqttQos.AT_MOST_ONCE
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectTLSAsDnc(dnc)


            subscribeMessage(dnc, subscribeUpTopicFilter, dncSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            connectAsDevice(device)

            publishMessage(device, topic, devicePublishQoS, message)

            Thread.sleep(15000)

            disconnectAndClearSession(device)
            disconnectAndClearSession(dnc)
        then:
            checkPoint.get() == 1
    }

}
