package com.jmqtt.mqtt


import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AbstractMqtt5Specification extends Specification{

    private static final Logger logger = LoggerFactory.getLogger(AbstractMqtt5Specification.class);

    /***
     * $share/{groupName}/{topicFilter}
     */
    public static String subscribeUpTopicFilter = '''$share/dnc/UP/+'''
    public static String publishV1TopicPrefix = "DOWN/"

    protected printReceivedMessage(Mqtt5Publish pub){
        logger.info("message is received by subscriber, publish={}, msg={}", pub, new String(pub.getPayloadAsBytes()))
    }

    protected printReceivedMessage(Mqtt3Publish pub){
        logger.info("message is received by subscriber, publish={}, msg={}", pub, new String(pub.getPayloadAsBytes()))
    }

    protected void publishMessage(Mqtt5Client publisher, topic, qos, message, expiry = 4_294_967_295){
        MqttClientUtils.publishMessage(publisher, topic, qos, message, expiry)
        sleep(100)
    }

    protected void publishRetainedMessage(Mqtt5Client publisher, topic, qos, message){
        publisher.toAsync().publishWith()
                .topic(topic)
                .qos(qos)
                .payload(message.getBytes())
                .retain(true)
                .send()
                .whenComplete(MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                .join()
        sleep(100)
    }

    protected void subscribeMessage(Mqtt5Client subscriber, topicFilter, qos, Closure<Void> callback){
        MqttClientUtils.subscribeMessage(subscriber, topicFilter, qos, callback)
        sleep(100)
    }

    protected void simpleConnect(Mqtt5Client client){
        client.toBlocking().connect()
        sleep(100)
    }

    protected void connectWithSessionExpiry(Mqtt5AsyncClient client, Integer sessionExpiryInSecond, boolean clearStart = true){
        client.connectWith()
                .sessionExpiryInterval(sessionExpiryInSecond)
                .cleanStart(clearStart)
                .send().join()
        sleep(100)
    }

    protected void reconnect(Mqtt5AsyncClient client, boolean clearStart = true){
        client.disconnect().join()
        sleep(100)
        client.connectWith()
                .cleanStart(clearStart)
                .send()
                .whenComplete(MqttClientUtils.getDefaultPublishWhenCompleteConsumer())
                .join()
        sleep(100)
    }

    protected void disconnectAndClearSession(Mqtt5Client... clients){
        //Sleep for the callback to execute
        Thread.sleep(1000)

        Arrays.stream(clients).forEach({ client ->
            logger.info("disconnect and clear session on client={}", client.getConfig().getClientIdentifier())
            client.toAsync()
                    .disconnectWith()
                    .sessionExpiryInterval(0)
                    .send().join()
        })
    }


}
