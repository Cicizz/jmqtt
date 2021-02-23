package com.jmqtt.mqtt.v3.acceptance.basic

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.jmqtt.mqtt.v3.acceptance.AbstractMqtt3Specification
import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

/***
 * Session Test
 *
 * Check the behavior when the session is enabled
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SessionTest extends AbstractMqtt3Specification{

    private static final Logger logger = LoggerFactory.getLogger(SessionTest.class);

    @Autowired
    @Qualifier("basicMqttServer")
    MqttServer basicServer;

    MqttClientFactory basicClientFactory;

    def setup(){
        basicClientFactory = new MqttClientFactory(basicServer);
    }

    /***
     * Check the subscriber session exists at the subscriber disconnect, when a message published, the message will be stored in the session.
     * After the subscriber re-connect with the same clientId before the session expires:
     * - it will receive the message at QoS 1 and 2
     * - the QoS 0 is OPTIONALLY being received. (depends on the broker implementation)
     */
    @Unroll
    def "subscribe first and disconnect, then publish with qos#qos, check the message is received when subscriber reconnect"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
//            def checkPointLatch = new CountDownLatch(1)
            def checkPoint = new AtomicInteger(0)

        when:
            connectWithClearSession(publisher)
            connectWithClearSession(subscriber)

            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    printReceivedMessage(pub)
                    checkPoint.addAndGet(1)
//                    checkPointLatch.countDown()
            })

            subscriber.toBlocking().disconnect()
            sleep(100)
            publishMessage(publisher, topic, qos, message)

            subscriber.connectWith().cleanSession(false).send().join()

            disconnectAndClearSession(publisher, subscriber)

        then:
//            checkPointLatch.await(2, TimeUnit.SECONDS)
            1 == checkPoint.get()

        where:
            qos                   | topic          | message
            MqttQos.AT_MOST_ONCE  | "myTopic_qos0" | "myMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "myTopic_qos1" | "myMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "myTopic_qos2" | "myMessage_qos2"

    }

    /***
     * Check if the subscriber session expires, then the message is gone when it reconnect.
     */
    @Unroll
    def "subscribe first and disconnect, then publish with qos#qos, check the message is not received after the session is expired"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def checkPoint = new AtomicInteger(0)
        when:
            connectWithClearSession(publisher)
            connectWithClearSession(subscriber)

            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    printReceivedMessage(pub)
                    checkPoint.addAndGet(1)
            })

            subscriber.toBlocking().disconnect()
            sleep(1000)

            logger.info("subscriber disconnected")
            publishMessage(publisher, topic, qos, message)

            sleep(8000)

            subscriber.connectWith().cleanSession(false).send().join()

            disconnectAndClearSession(publisher, subscriber)

        then:
            0 == checkPoint.get()

        where:
            qos                   | topic          | message
            MqttQos.AT_MOST_ONCE  | "myTopic_qos0" | "myMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "myTopic_qos1" | "myMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "myTopic_qos2" | "myMessage_qos2"

    }


    /***
     * Check when the subscriber reconnect, multiple messages of the same topic will be received after it reconnect
     */
    @Unroll
    def "subscribe first and disconnect, then publish 2 msgs with qos#qos, check the message is received when subscriber reconnect"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def checkPoint = new AtomicInteger(0)
        when:
            connectWithClearSession(publisher)
            connectWithClearSession(subscriber)

            subscribeMessage(subscriber, topic, qos, {
                pub ->
                    printReceivedMessage(pub)
                    checkPoint.addAndGet(1)
            })

            subscriber.toBlocking().disconnect()

            publishMessage(publisher, topic, qos, message)
            publishMessage(publisher, topic, qos, message + "_2")
            subscriber.connectWith().cleanSession(false).send().join()

            disconnectAndClearSession(publisher, subscriber)

        then:
            2 == checkPoint.get()

        where:
            qos                   | topic          | message
            MqttQos.AT_MOST_ONCE  | "myTopic_qos0" | "myMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "myTopic_qos1" | "myMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "myTopic_qos2" | "myMessage_qos2"

    }

}
