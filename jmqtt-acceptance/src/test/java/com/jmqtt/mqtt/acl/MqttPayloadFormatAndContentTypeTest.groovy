package com.jmqtt.mqtt.acl


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttAclClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MqttPayloadFormatAndContentTypeTest extends AbstractAclMqttSpecification{

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttAclClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttAclClientFactory(aclServer);
    }

    def "contentType Byte:"(){
        given: "topic:DOWN/{CLIENTID}"
            def clientId = MqttClientFactory.getUUID()
            def topic = "DOWN/" + clientId
        and: "mqtt auth and connect"

            def clientV3 = aclClientFactory.createMqtt3Client(clientId)
            clientV3.toBlocking().connectWith().send()

            def clientV5 = aclClientFactory.createMqtt5Client()
            connectAsDnc(clientV5)

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

            clientV5.toAsync().publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes())
                    .contentType("application/json")
                    .messageExpiryInterval(1000L)
                    .send()
                    .whenComplete(MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                    .join()
            sleep(100)
        then:
            message == payload.toString()
        where:
            message << ["message v5 to v3"]

    }
}
