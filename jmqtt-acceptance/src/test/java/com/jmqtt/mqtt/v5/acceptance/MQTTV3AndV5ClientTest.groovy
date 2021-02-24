package com.jmqtt.mqtt.v5.acceptance


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttAclClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * scenario test:
 * 1.Check data format ( v3.1 & v5.0 )
 * 2.Check the frequency of duplicate published message from device
 *
 * The ACL in the EMQ X is
 * %% test cloud rule
 *{allow, {user, "testCloud"},pubsub , ["$SYS/#",  "#"]}.
 *
 * %% device subscribe rule :{DSN}/DOWN
 *{allow, all, subscribe, ["DOWN/%c"]}.
 * %% device publish rule :{DSN}/UP
 *{allow, all, publish, ["UP/%c"]}.
 */
class MQTTV3AndV5ClientTest extends AbstractAclMqttSpecification{

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttAclClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttAclClientFactory(aclServer);
    }

    def "Scenario 1: v3 subscribe v5 (v5 publish to v3)"(){
        given: "topic:{CLIENTID}/DOWN"
            def clientId = MqttClientFactory.getUUID()
            def topic = "DOWN/" + clientId
        and: "mqtt auth and connect"

            def clientV3 = aclClientFactory.createMqtt3Client(clientId)
            clientV3.toBlocking().connectWith().send()

            def dncClientV5 = aclClientFactory.createMqtt5Client()
            connectAsDnc(dncClientV5)

        when: "v3 subscribe topic"

            def payload = new StringBuffer()

            clientV3.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ Mqtt3Publish publish ->
                        payload.append(new String(publish.getPayloadAsBytes()))
                    })
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join()
            sleep(100)
        and: "v5 publish message"
            publishMessage(dncClientV5, topic, MqttQos.AT_LEAST_ONCE, message, 1000L)
        then:
            message == payload.toString()
        where:
            message << ["message v5 to v3"]

    }


    def "Scenario 2: v5 subscribe v3(v3 publish to v5)"(){
        given: "topic:UP/{CLIENTID}"
            def clientDsn = MqttClientFactory.getUUID()
            def topic = "UP/" + clientDsn
        and: "mqtt auth and connect"

            def dncClientV5 = aclClientFactory.createMqtt5Client()
            connectAsDnc(dncClientV5)


            def clientV3 = aclClientFactory.createMqtt3Client(clientDsn)
            clientV3.toBlocking().connectWith().send()

        when: "v5 subscribe topic"

            def payload = new StringBuffer()

            dncClientV5.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ Mqtt5Publish publish ->
                        payload.append(new String(publish.getPayloadAsBytes()))
                    })
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join()
            sleep(100)
        and: "v3 publish message"
            clientV3.toAsync().publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes())
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                    .join()
            sleep(100)
        then:
            message == payload.toString()
        where:
            message << ["message v3 to v5"]
    }

    def "Scenario 3 : Maximum packet size,v5 subscribe v3(v3 publish to v5)"(){
        given: "topic:UP/{CLIENTID}"
            def clientDsn = MqttClientFactory.getUUID()
            def topic = "UP/" + clientDsn
        and: "mqtt auth and connect"
            MqttUtf8StringImpl mqttUtf8String = new MqttUtf8StringImpl(dncUserName)
            def simpleAuth = new MqttSimpleAuth(mqttUtf8String, null)
            def mqtt5Connect = getMqtt5Connect(simpleAuth)

            def clientV5 = aclClientFactory.createMqtt5Client()
            clientV5.toBlocking().connect(mqtt5Connect)

            def newClientV5 = aclClientFactory.createMqtt5Client()
            connectAsDnc(newClientV5)

            def clientV3 = aclClientFactory.createMqtt3Client(clientDsn)
            clientV3.toBlocking().connectWith().send()

        when: "v5 subscribe topic"

            def payload = new StringBuffer()

            def anotherPayload = new StringBuffer()

            clientV5.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ Mqtt5Publish publish ->
                        payload.append(new String(publish.getPayloadAsBytes()))
                    })
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join()
        and: " another v5 client subscribe with out limit max packet size"
            newClientV5.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ Mqtt5Publish publish ->
                        anotherPayload.append(new String(publish.getPayloadAsBytes()))
                    })
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join()
            sleep(100)
        and: "v3 publish message"
            clientV3.toAsync().publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes())
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                    .join()
            sleep(100)
        then:
            expectMessage == payload.toString()
        where:
            message                                                                        | expectMessage | anotherExpect
            "message v3 to v5"                                                             | message       | message
            "message v3 to v5,message v3 to v5,message v3 to v5," +
                    "message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5," +
                    "message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5," +
                    "message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5," +
                    "message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5," +
                    "message v3 to v5,message v3 to v5,message v3 to v5,message v3 to v5," | ""            | message
    }

    def getMqtt5Connect(MqttSimpleAuth simpleAuth){
        MqttConnectRestrictions defaultRestrictions = new MqttConnectRestrictions(MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, MqttConnectRestrictions.DEFAULT_SEND_MAXIMUM, 128,
                MqttConnectRestrictions.DEFAULT_SEND_MAXIMUM_PACKET_SIZE, MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM, MqttConnectRestrictions.DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM,
                MqttConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION, MqttConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION);
        new MqttConnect(Mqtt5Connect.DEFAULT_KEEP_ALIVE, Mqtt5Connect.DEFAULT_CLEAN_START, Mqtt5Connect.DEFAULT_SESSION_EXPIRY_INTERVAL,
                defaultRestrictions, simpleAuth, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }
}
