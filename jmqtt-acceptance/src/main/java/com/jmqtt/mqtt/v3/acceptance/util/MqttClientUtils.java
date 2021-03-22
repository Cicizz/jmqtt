package com.jmqtt.mqtt.v3.acceptance.util;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3PubAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/***
 * Provide the utilities for the common used consumer, supplier...etc
 */
public class MqttClientUtils {
    public static final Logger log = LoggerFactory.getLogger(MqttClientUtils.class);

    public static Mqtt5SubAck subscribeMessage(Mqtt5Client subscriber,String topicFilter, MqttQos qos, Consumer<Mqtt5Publish> callback){
        return subscriber.toAsync()
            .subscribeWith()
            .topicFilter(topicFilter)
            .qos(qos)
            .callback(callback)
            .send()
            .whenComplete(getDefaultSubscribeWhenCompleteConsumer())
            .join();
    }

    public static Mqtt5PublishResult publishMessage(Mqtt5Client publisher, String topic, MqttQos qos,String message,Long expiry){
        log.debug("publish message : {}", message);
        return publisher.toAsync().publishWith()
            .topic(topic)
            .qos(qos)
            .payload(message.getBytes())
            .messageExpiryInterval(expiry)
            .send()
            .whenComplete(getDefaultPublishWhenCompleteConsumer())
            .join();

    }
    public static Mqtt3SubAck subscribeMessage(Mqtt3Client subscriber, String topicFilter, MqttQos qos, Consumer<Mqtt3Publish> callback){
        return subscriber.toAsync()
                .subscribeWith()
                .topicFilter(topicFilter)
                .qos(qos)
                .callback(callback)
                .send()
                .whenComplete(getDefaultSubscribeWhenCompleteConsumer())
                .join();
    }

    public static Mqtt3Publish publishMessage(Mqtt3Client publisher, String topic, MqttQos qos, String message, Long expiry){
        log.debug("publish message : {}", message);
        return publisher.toAsync().publishWith()
                .topic(topic)
                .qos(qos)
                .payload(message.getBytes())
                .send()
                .whenComplete(getDefaultPublishWhenCompleteConsumer())
                .join();

    }

    public static <T> void defaultWhenComplete(T t, Throwable ex) {
        if (ex != null) {
            log.error("ack = {}", t);
            log.error(ex.getMessage(), ex);
        } else {
            log.info("ack message = {}", t);
        }
    }

    public static <T> BiConsumer<T, ? super Throwable> getDefaultPublishWhenCompleteConsumer() {
        return (T t, Throwable ex) -> {

            if (ex != null) {
                String errorCode = "";
                if(ex instanceof Mqtt5PubAckException){
                    errorCode = ((Mqtt5PubAckException)ex).getMqttMessage().getReasonCode().toString();
                }else if(ex instanceof Mqtt3PubAckException){
                    errorCode = ((Mqtt3PubAckException)ex).getMqttMessage().toString();
                }
                log.error("message is not published, Error in the PubAck : code={}",errorCode, ex );
            } else {
                log.info("message is published, PubAck message={}", t);
            }
        };
    }

    public static <T> BiConsumer<T, ? super Throwable> getDefaultSubscribeWhenCompleteConsumer() {
        return (T t, Throwable ex) -> {
            if (ex != null) {
                String errorCode = "";
                if(ex instanceof Mqtt5SubAckException){
                    errorCode = ((Mqtt5SubAckException)ex).getMqttMessage().getReasonCodes().toString();
                }else if(ex instanceof Mqtt3SubAckException){
                    errorCode = ((Mqtt3SubAckException)ex).getMqttMessage().getReturnCodes().toString();
                }
                log.error("message is not subscribed, Error in the SubAck : code={}",errorCode, ex );

            } else {
                log.info("message is subscribed, SubAck message={}", t);

            }
        };
    }


}
