package com.jmqtt.mqtt.v5.acceptance


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

import java.nio.ByteBuffer
import java.util.function.BiConsumer
/**
 * scenario test:
 * 1.Check data format ( v3.1 & v5.0 )
 * 2.Check the frequency of duplicate published message from device
 * <p>
 * The ACL in the EMQ X is
 * %% test cloud rule
 *{allow, {user, "testCloud"},pubsub , ["$SYS/#",  "#"]}.
 * <p>
 * %% device subscribe rule :{DSN}/DOWN
 *{allow, all, subscribe, ["DOWN/%c"]}.
 * %% device publish rule :{DSN}/UP
 *{allow, all, publish, ["UP/%c"]}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class V3CoorelationAndResponseTest extends AbstractAclMqttSpecification{

    static final Logger logger = LoggerFactory.getLogger(V3CoorelationAndResponseTest.class);

    String message = "message cloud to client";

    String respMsg = "client response to cloud";

    String correlation = "req and resp correlation";

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttClientFactory(aclServer);
    }


    /***
     * Scenario 1 : Check nobody can subscribe topic "UP/{DSN}" except DNC
     */
    def "testReqResV3"(){
        given:
            String clientId = aclClientFactory.getUUID();

            String topic = "DOWN/" + clientId;

            String responseTopic = "UP/" + clientId;

            Mqtt3Client clientV3 = aclClientFactory.createMqtt3Client(clientId);
            clientV3.toBlocking().connectWith().send();

            Mqtt5Client testCloudClient = aclClientFactory.createMqtt5Client(aclClientFactory.getUUID());
            connectAsDnc(testCloudClient)


            StringBuffer payload = new StringBuffer();
            StringBuffer expectResponseTopic = new StringBuffer();

        when:
            // client sub request and publish resp
            clientV3.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ publish ->
                        payload.append(new String(publish.getPayloadAsBytes()));


                        expectResponseTopic.append(responseTopic);
                        clientV3.toAsync().publishWith()
                                .topic(responseTopic)
                                .payload(respMsg.getBytes())
                                .send()
                                .whenComplete((BiConsumer<? super Mqtt3Publish, ? super Throwable>) MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                                .join();
                    })
                    .send()
                    .whenComplete((BiConsumer<? super Mqtt3SubAck, ? super Throwable>) MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join();

            // cloud sub resp and publish request

            StringBuffer expectResponse = new StringBuffer();
            StringBuffer expectCorrelation = new StringBuffer();

            testCloudClient.toAsync()
                    .subscribeWith()
                    .topicFilter(responseTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ publish ->
                        byte[] payloadAsBytes = publish.getPayloadAsBytes();
                        Optional<ByteBuffer> correlationData = publish.getCorrelationData();


                        expectResponse.append(new String(payloadAsBytes));
                        ByteBuffer correlationBuffer = correlationData.get();

                        final byte[] binary = new byte[correlationBuffer.remaining()];
                        correlationBuffer.duplicate().get(binary);
                        expectCorrelation.append(new String(binary));

                    }).send()
                    .whenComplete((BiConsumer<? super Mqtt5SubAck, ? super Throwable>) MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                    .join();
            testCloudClient.toAsync().publishWith()
                    .topic(topic)
                    .responseTopic(responseTopic)
                    .correlationData(correlation.getBytes())
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes())
                    .send()
                    .whenComplete((BiConsumer<? super Mqtt5PublishResult, ? super Throwable>) MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                    .join();

            Thread.sleep(1000L);
        then:
            //resp check
            assert expectResponse.toString() == respMsg
    }


}
