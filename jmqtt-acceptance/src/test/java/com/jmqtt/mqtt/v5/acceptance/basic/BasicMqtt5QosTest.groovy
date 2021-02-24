package com.jmqtt.mqtt.v5.acceptance.basic


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.AbstractMqtt5Specification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

/***
 * Basic MQTT Test
 *  - Session is always default value ( cleared after offline )
 *  - No Retained flag on published message
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BasicMqtt5QosTest extends AbstractMqtt5Specification{

    static final Logger logger = LoggerFactory.getLogger(BasicMqtt5QosTest.class)

    @Autowired
    @Qualifier("basicMqttServer")
    MqttServer basicServer

    MqttClientFactory basicClientFactory = new MqttClientFactory(basicServer)

    def setup(){
        basicClientFactory = new MqttClientFactory(basicServer)
    }
    /***
     * Check when the subscriber online, message will be delivered for all QoS
     */
    @Unroll
    def "subscribe first, receive the message, publisher qos is #qos, subscriber qos is #qos"(){
        given:
            def subscriber = basicClientFactory.createMqtt5Client()
            def publisher = basicClientFactory.createMqtt5Client()
            def checkPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)

            subscribeMessage(subscriber, topic, qos, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            publishMessage(publisher, topic, qos, message)
            disconnectAndClearSession(publisher, subscriber)
        then:
            1 == checkPoint.get()

        where:
            qos                   | topic        | message
            MqttQos.AT_MOST_ONCE  | "myTopic0_0" | "myMessage0_0"
            MqttQos.AT_LEAST_ONCE | "myTopic1_0" | "myMessage1_0"
            MqttQos.EXACTLY_ONCE  | "myTopic2_0" | "myMessage2_0"

    }

    /***
     * Check the published message cannot be received by the subscription afterward.
     * In any QoS.
     */
    @Unroll
    def "publish first then subscribe,  qos is #qos, subscriber qos is #qos"(){
        given:
            def subscriber = basicClientFactory.createMqtt5Client().toAsync()
            def publisher = basicClientFactory.createMqtt5Client().toAsync()
            def checkPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)
            publishMessage(publisher, topic, qos, message)
            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    checkPoint.addAndGet(1)
            })
            disconnectAndClearSession(publisher, subscriber)
        then:
            0 == checkPoint.get()


        where:
            qos                   | topic        | message
            MqttQos.AT_MOST_ONCE  | "myTopic0_1" | "myMessage0_1"
            MqttQos.AT_LEAST_ONCE | "myTopic1_1" | "myMessage1_1"
            MqttQos.EXACTLY_ONCE  | "myTopic2_1" | "myMessage2_1"

    }


}
