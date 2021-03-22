package com.jmqtt.mqtt.v3.acceptance.basic

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.jmqtt.mqtt.v3.acceptance.AbstractMqtt3Specification
import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

/***
 * To send clearStart, the client must reconnect to the broker to send CONNECT message
 *
 * It will disconnect the current worker by
 *  - disconnect and connect again
 *  - create another client with the same identifier
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClearSessionTest extends AbstractMqtt3Specification{

    @Autowired
    @Qualifier("basicMqttServer")
    MqttServer basicServer;

    MqttClientFactory basicClientFactory;

    def setup(){
        basicClientFactory = new MqttClientFactory(basicServer)
    }
    @Unroll
    def "check message is cleared when clear session, by reconnect the subscriber"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def checkPoint = new AtomicInteger(0)
        when:
            publisher.connectWith()
                    .send().join()
            subscriber.connectWith()
                    .send().join()

            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    printReceivedMessage(pub)
                    checkPoint.addAndGet(1)
            })

            subscriber.toBlocking().disconnect()
            sleep(100)
            publishMessage(publisher, topic, qos, message)

            simpleConnect(subscriber)

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
