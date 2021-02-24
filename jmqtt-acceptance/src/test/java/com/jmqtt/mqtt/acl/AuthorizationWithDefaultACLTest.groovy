package com.jmqtt.mqtt.acl


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

import java.util.concurrent.CompletionException
import java.util.concurrent.atomic.AtomicInteger
/***
 * Scenario test : ACL authorization.
 *
 * To validate if the topic validation works.
 * Follow the documentation : https://github.com/testAsia/devicenotificationservice/wiki/Topics
 *
 * The CONNECT packet of DNC and device is defined in the design document:
 * https://github.com/testAsia/devicenotificationservice/wiki/MQTT-Architecture
 *
 * The ACL in the EMQ X is
 *{allow, {user, "dnc"}, pubsub, ["#"]}.{allow, all, publish, ["UP/{DSN}"]}.{allow, all, subscribe, ["DOWN/{DSN}"]}.{deny, all}.
 *
 *
 *
 * Upstream
 * Scenario 1 : Check nobody can subscribe topic "UP/{DSN}" except DNC
 * Scenario 2 : Check when device follow the format of the topic "UP/{DSN}", it can publish the message, and DNC will receive the message
 * Scenario 3 : Check when the device doesn't follow the topic format "UP/{DSN}", it will receive and error code by the PUBACK.
 *
 * Downstream:
 * Scenario 4 : Check the device can only subscribe its own topic of format "DOWN/{DSN}", and the DSN is the clientId of the device.
 * Scanario 5 : Check when the DNC publish a message, only the specific device receive the message.
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuthorizationWithDefaultACLTest extends AbstractAclMqttSpecification{

    static final Logger logger = LoggerFactory.getLogger(AuthorizationWithDefaultACLTest.class);

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttClientFactory(aclServer)
    }


    /***
     * Scenario 1 : Check nobody can subscribe topic "UP/{DSN}" except DNC
     */
    def "Scenario 1 : check DNC subscription permission"(){
        given:
            def topic = subscribeUpTopicFilter
        and:

            //dnc Info
            def notDnc = aclClientFactory.createMqtt5Client("notDnc", "password").toAsync()
            def dncSubscribeQoS = MqttQos.AT_LEAST_ONCE
        and:
            def nonDncCheckPoint = new AtomicInteger(0)
        when:
            simpleConnect(notDnc)

            subscribeMessage(notDnc, topic, dncSubscribeQoS, { Mqtt5Publish pub ->
                nonDncCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            disconnectAndClearSession(notDnc)
        then:
            def ex = thrown(CompletionException.class)
            assert Mqtt5SubAckReasonCode.NOT_AUTHORIZED == ((Mqtt5SubAckException)ex.cause).getMqttMessage().reasonCodes[0]
    }

    /***
     * Scenario 2 : Check when device follow the format of the topic "UP/{DSN}", it can publish the message, and DNC will receive the message
     */
    def "Scenario 2 : check upstream ACL of the Device"(){
        given:
            //general info
            def message = "upstream_happy_path"
        and:
            //device Info
            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_1"
            def topic = "UP/" + deviceDSN
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()
        and:
            //dnc Info
            def dnc = aclClientFactory.createMqtt5Client().toAsync()
            def dncSubscribeQoS = MqttQos.AT_MOST_ONCE
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectAsDnc(dnc)
            connectAsDevice(device)

            subscribeMessage(dnc, subscribeUpTopicFilter, dncSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            publishMessage(device, topic, devicePublishQoS, message)
            Thread.sleep(10000)
            disconnectAndClearSession(device, dnc)
        then:
            1 == checkPoint.get()
    }


    /***
     * Check when the device doesn't follow the topic format "UP/{DSN}", it will receive and error code by the PUBACK.
     *
     */
    def "Scenario 3 : device cannot publish any message except 'UP/' "(){
        given:
            //general info
            def topic = "Illegal_Topic"
            def message = "cannot be delivered"
        and:
            //device Info
            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_2"
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()

            //dnc Info
            def dnc = aclClientFactory.createMqtt5Client().toAsync()
            def dncSubscribeQoS = MqttQos.AT_MOST_ONCE
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectAsDnc(dnc)
            connectAsDevice(device)

            subscribeMessage(dnc, subscribeUpTopicFilter, dncSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            publishMessage(device, topic, devicePublishQoS, message)

            disconnectAndClearSession(device, dnc)
        then:
            def ex = thrown(CompletionException.class)
            Mqtt5PubAckException errorAck = ex.getCause()
            Mqtt5PubAckReasonCode.NOT_AUTHORIZED == errorAck.getMqttMessage().getReasonCode()
    }

    /***
     * Scenario 4 : Check the device can subscribe its own topic of format "DOWN/{DSN}", and the DSN is the clientId of the device.
     *
     */
    def "Scenario 4 : device can subscribe its own topic "(){
        given:
            //general info
            def message = "V2 CMDs"
        and:
            //device Info
            def deviceSubscribeQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_4"
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()

            //dnc Info
            def dnc = aclClientFactory.createMqtt5Client().toAsync()
            def dncPublishQoS = MqttQos.AT_LEAST_ONCE
            def topic = "DOWN/" + deviceDSN
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectAsDnc(dnc)
            connectAsDevice(device)

            subscribeMessage(device, topic, deviceSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            publishMessage(dnc, topic, dncPublishQoS, message)

            disconnectAndClearSession(device, dnc)
        then:
            1 == checkPoint.get()
    }

    /***
     * Scenario 4 : Check the device can subscribe its own topic of format "DOWN/{DSN}", and the DSN is the clientId of the device.
     *
     */
    def "Scenario 4.1 : device cannot subscribe topic of other DSN "(){
        given:
            //general info
            def message = "V2 CMDs"
        and:
            //device Info
            def deviceSubscribeQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_4"
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()

            def topic = "DOWN/" + "OTHER_DSN"
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectAsDevice(device)

            subscribeMessage(device, topic, deviceSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            disconnectAndClearSession(device)
        then:
            def ex = thrown(CompletionException.class)
            Mqtt5SubAckException errorAck = ex.getCause()
            Mqtt5SubAckReasonCode.NOT_AUTHORIZED in errorAck.getMqttMessage().getReasonCodes()

    }


    /***
     * Scanario 5 : Check when the DNC publish a message, only the specific device receive the message.
     */
    def "Scenario 5 : other device will not receive the message of other device  "(){
        given:
            //general info
            def message = "V2 CMDs"
        and:
            //device Info
            def deviceSubscribeQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_5"
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()
            def subscribeTopic = "DOWN/" + deviceDSN

            def deviceDSN2 = "device_NO_5_2"
            def device2 = aclClientFactory.createMqtt5Client(deviceDSN2).toAsync()
            def subscribeTopic2 = "DOWN/" + deviceDSN2

            //dnc Info
            def dnc = aclClientFactory.createMqtt5Client().toAsync()
            def dncPublishQoS = MqttQos.AT_LEAST_ONCE
            def topic = "DOWN/" + deviceDSN
        and:
            def checkPoint = new AtomicInteger(0)
            def checkPoint2 = new AtomicInteger(0)
        when:
            connectAsDnc(dnc)
            connectAsDevice(device)
            connectAsDevice(device2)

            subscribeMessage(device, subscribeTopic, deviceSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            subscribeMessage(device2, subscribeTopic2, deviceSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint2.addAndGet(1)
                printReceivedMessage(pub)
            })

            publishMessage(dnc, topic, dncPublishQoS, message)

            disconnectAndClearSession(device, dnc)
        then:
            1 == checkPoint.get()
            0 == checkPoint2.get()
    }
}
