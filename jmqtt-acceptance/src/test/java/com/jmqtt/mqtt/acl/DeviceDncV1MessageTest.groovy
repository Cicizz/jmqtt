package com.jmqtt.mqtt.acl


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DeviceDncV1MessageTest extends AbstractAclMqttSpecification{

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttClientFactory(aclServer)
    }

    def "scenario 1: authentication test request/response"(){
        given: "device Info"
            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_1"
            def upTopic = "UP/" + deviceDSN
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()
            //device Info
            def deviceSubscribeQoS = MqttQos.AT_LEAST_ONCE

            def downTopic = "DOWN/" + deviceDSN

            def checkPoint = new AtomicInteger(0)

            connectAsDevice(device)


        when:
            publishMessage(device, upTopic, devicePublishQoS, message)
        and:

            subscribeMessage(device, downTopic, deviceSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.incrementAndGet()
                printReceivedMessage(pub)
            })
            Thread.sleep(1000)
            disconnectAndClearSession(device)

        then:
            notThrown(Exception)

        where:
            message        | _
            "device_NO_1 " | ""
    }
}
