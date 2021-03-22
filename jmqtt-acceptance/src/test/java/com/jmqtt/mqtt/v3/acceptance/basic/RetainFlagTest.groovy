package com.jmqtt.mqtt.v3.acceptance.basic

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.jmqtt.mqtt.v3.acceptance.AbstractMqtt3Specification
import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RetainFlagTest extends AbstractMqtt3Specification{

    @Autowired
    @Qualifier("basicMqttServer")
    MqttServer basicServer;

    MqttClientFactory basicClientFactory;

    def setup(){
        basicClientFactory = new MqttClientFactory(basicServer);
    }

    @Unroll
    def "check the retained message is not removed after the message is consumed by another subscriber"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def newSubscriber = basicClientFactory.createMqtt3Client().toAsync()
        and:
            def subscriberCheckPoint = new AtomicInteger(0)
            def newSubscriberCheckPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)
            simpleConnect(newSubscriber)


            publishRetainedMessage(publisher, "UP/" + topic, qos, message)

            subscribeMessage(subscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                subscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            //subscribe with new subscribe
            subscribeMessage(newSubscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                newSubscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            disconnectAndClearSession(publisher, subscriber, newSubscriber)


        then:
            1 == subscriberCheckPoint.get()
            1 == newSubscriberCheckPoint.get()


        where:
            qos                   | topic                 | message
            MqttQos.AT_MOST_ONCE  | "clearSessionTest1_0" | "clearedMessage_0"
            MqttQos.AT_LEAST_ONCE | "clearSessionTest1_1" | "clearedMessage_1"
            MqttQos.EXACTLY_ONCE  | "clearSessionTest1_2" | "clearedMessage_2"

    }

    /***
     * When all the session is cleared, the retained message is still in the broker and can be received.
     *
     * => To verify the retained message is not stored in any session
     */
    @Unroll
    def "check retained message is not cleared when subscriber and publisher clear session, and received by a new subscriber"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def newSubscriber = basicClientFactory.createMqtt3Client().toAsync()
        and:
            def subscriberCheckPoint = new AtomicInteger(0)
            def newSubscriberCheckPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)
            simpleConnect(newSubscriber)


            publishRetainedMessage(publisher, "UP/" + topic, qos, message)
            disconnectAndClearSession(publisher)

            subscribeMessage(subscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                subscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            disconnectAndClearSession(subscriber)

            //subscribe with new subscribe
            subscribeMessage(newSubscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                newSubscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            disconnectAndClearSession(newSubscriber)


        then:
            1 == subscriberCheckPoint.get()
            1 == newSubscriberCheckPoint.get()


        where:
            qos                   | topic                 | message
            MqttQos.AT_MOST_ONCE  | "clearSessionTest1_0" | "clearedMessage_0"
            MqttQos.AT_LEAST_ONCE | "clearSessionTest1_1" | "clearedMessage_1"
            MqttQos.EXACTLY_ONCE  | "clearSessionTest1_2" | "clearedMessage_2"

    }

    /***
     *  According to the spec, when the retained message payload is empty, it's actually delete the message.
     *
     *  Caution : The retained message will be resent (For EMQ X) before it is deleted, it's not in the spec!!
     *
     */
    @Unroll
    def "check the retained message is removed by publish empty retained message"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
            def newSubscriber = basicClientFactory.createMqtt3Client().toAsync()
        and:
            def subscriberCheckPoint = new AtomicInteger(0)
            def newSubscriberCheckPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)
            simpleConnect(newSubscriber)


            publishRetainedMessage(publisher, "UP/" + topic, qos, message)

            subscribeMessage(subscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                subscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            publishRetainedMessage(publisher, "UP/" + topic, qos, "")


            //subscribe with new subscribe
            subscribeMessage(newSubscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                newSubscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            disconnectAndClearSession(publisher, subscriber, newSubscriber)

        then:
            2 == subscriberCheckPoint.get()
            0 == newSubscriberCheckPoint.get()

        where:
            qos                   | topic                 | message
            MqttQos.AT_MOST_ONCE  | "clearSessionTest1_0" | "clearedMessage_0"
            MqttQos.AT_LEAST_ONCE | "clearSessionTest1_1" | "clearedMessage_1"
            MqttQos.EXACTLY_ONCE  | "clearSessionTest1_2" | "clearedMessage_2"

    }

    /*** No session enabled, check the retained message is overridden.
     *
     */
    @Unroll
    def "check the retained message can only have latest one message and can be overridden"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
        and:
            def subscriberCheckPoint = new AtomicInteger(0)
        when:
            simpleConnect(publisher)
            simpleConnect(subscriber)

            publishRetainedMessage(publisher, "UP/" + topic, qos, message)
            publishRetainedMessage(publisher, "UP/" + topic, qos, newMessage)

            subscribeMessage(subscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                subscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
                assert newMessage == new String(pub.getPayloadAsBytes())
            })

            disconnectAndClearSession(publisher, subscriber)

        then:
            1 == subscriberCheckPoint.get()

        where:
            qos                   | topic                 | message            | newMessage
            MqttQos.AT_MOST_ONCE  | "clearSessionTest1_0" | "clearedMessage_0" | "newMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "clearSessionTest1_1" | "clearedMessage_1" | "newMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "clearSessionTest1_2" | "clearedMessage_2" | "newMessage_qos2"

    }

    /*** when session enabled, check the retained message is overridden and only receive once.
     *
     *   Caution : There will be 3 messages received by the reconnected client.
     *
     *   The broker target to deliver message is mainly the subscription session.
     *   If the session is alive, the retained message will keep sending to the session.
     *   And when the subscriber is back CONNECT, the retained message is sent to the subscriber again!!
     *
     */
    @Unroll
    def "check the retained message can only have latest one message and can be overridden for resumed session"(){
        given:
            def subscriber = basicClientFactory.createMqtt3Client().toAsync()
            def publisher = basicClientFactory.createMqtt3Client().toAsync()
        and:
            def subscriberCheckPoint = new AtomicInteger(0)
        when:
            connectWithClearSession(publisher)
            connectWithClearSession(subscriber)

            subscribeMessage(subscriber, "UP/" + topic, qos, { Mqtt3Publish pub ->
                subscriberCheckPoint.addAndGet(1)
                printReceivedMessage(pub)
            })
            subscriber.toBlocking().disconnect()

            publishRetainedMessage(publisher, "UP/" + topic, qos, message)
            publishRetainedMessage(publisher, "UP/" + topic, qos, newMessage)

            connectWithClearSession(subscriber, false)


            disconnectAndClearSession(publisher, subscriber)

        then:
            3 == subscriberCheckPoint.get()

        where:
            qos                   | topic                 | message            | newMessage
            MqttQos.AT_MOST_ONCE  | "clearSessionTest1_0" | "clearedMessage_0" | "newMessage_qos0"
            MqttQos.AT_LEAST_ONCE | "clearSessionTest1_1" | "clearedMessage_1" | "newMessage_qos1"
            MqttQos.EXACTLY_ONCE  | "clearSessionTest1_2" | "clearedMessage_2" | "newMessage_qos2"

    }
}
