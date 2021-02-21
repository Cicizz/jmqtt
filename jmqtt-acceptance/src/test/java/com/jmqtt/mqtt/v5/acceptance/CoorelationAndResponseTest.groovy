package com.jmqtt.mqtt.v5.acceptance


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.datatypes.MqttTopic
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CoorelationAndResponseTest extends AbstractAclMqttSpecification{

    static final Logger logger = LoggerFactory.getLogger(CoorelationAndResponseTest.class);

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
     * Scenario 1 : check coorelation and response
     */
    def "check coorelation and response topic "(){
        given:
            String clientId = aclClientFactory.getUUID();

            String topic = "DOWN/" + clientId;

            String responseTopic = "UP/" + clientId;

            Mqtt5Client clientV5 = aclClientFactory.createMqtt5Client(clientId);
            connectAsDevice(clientV5)

            Mqtt5Client testCloudClient = aclClientFactory.createMqtt5Client();
            connectAsDnc(testCloudClient)


            StringBuffer payload = new StringBuffer();
            StringBuffer expectResponseTopic = new StringBuffer();

        when:
            // client sub request and publish resp
            clientV5.toAsync()
                    .subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback({ publish ->
                        payload.append(new String(publish.getPayloadAsBytes()));

                        Optional<ByteBuffer> correlationData = publish.getCorrelationData();
                        Optional<MqttTopic> responseTopic1 = publish.getResponseTopic();

                        expectResponseTopic.append(responseTopic1.get());
                        clientV5.toAsync().publishWith()
                                .topic(responseTopic1.get())
                                .correlationData(correlationData.get())
                                .payload(respMsg.getBytes())
                                .send()
                                .whenComplete((BiConsumer<? super Mqtt5PublishResult, ? super Throwable>) MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
                                .join();
                    })
                    .send()
                    .whenComplete((BiConsumer<? super Mqtt5SubAck, ? super Throwable>) MqttClientUtils.getDefaultSubscribeWhenCompleteConsumer())
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
            assert expectResponseTopic.toString() == responseTopic

            //resp check
            assert expectResponse.toString() == respMsg
            //correlation check
            assert expectCorrelation.toString() == correlation
    }

}
