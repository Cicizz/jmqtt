package com.jmqtt.mqtt.v5.acceptance.basic


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.jmqtt.mqtt.AbstractMqtt5Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

/***
 * To validate the message expiry works
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MessageExpiryTest extends AbstractMqtt5Specification{

    @Autowired
    @Qualifier("basicMqttServer")
    MqttServer basicServer;

    MqttClientFactory basicClientFactory;

    def setup(){
        basicClientFactory = new MqttClientFactory(basicServer);
    }

    @Unroll
    def "check the message expires when the session is enabled"(){
        given:
            def subscriber = basicClientFactory.createMqtt5Client().toAsync()
            def publisher = basicClientFactory.createMqtt5Client().toAsync()
            def checkPoint = new AtomicInteger(0)
        when:
            connectWithSessionExpiry(publisher, 30)
            connectWithSessionExpiry(subscriber, 30)

            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    printReceivedMessage(pub)
                    checkPoint.addAndGet(1)
            })

            subscriber.toBlocking().disconnect()
            sleep(100)
            publishMessage(publisher, "UP/" + topic, qos, message, 2)
            sleep(3000)
            subscriber.connectWith().cleanStart(false).send().join()

            disconnectAndClearSession(publisher, subscriber)

        then:
            0 == checkPoint.get()

        where:
            qos                   | topic          | message
            MqttQos.AT_MOST_ONCE  | "myTopic_qos0" | "myMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "myTopic_qos1" | "myMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "myTopic_qos2" | "myMessage_qos2"

    }


}
