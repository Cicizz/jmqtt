package com.jmqtt.mqtt.acl


import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.jmqtt.mqtt.AbstractAclMqttSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

import java.util.concurrent.atomic.AtomicInteger
/***
 * Check what can be retrieved by DNC
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DNCTest extends AbstractAclMqttSpecification{

    static final Logger logger = LoggerFactory.getLogger(DNCTest.class);

    @Autowired
    @Qualifier("aclMqttServer")
    MqttServer aclServer;

    MqttClientFactory aclClientFactory;

    def setup(){
        aclClientFactory = new MqttClientFactory(aclServer);
    }

    /***
     * Scenario 1 : DNC to subscribe everything
     */
    def "Scenario 1 : check DNC subscription permission"(){
        given:
            //general info
            def message = "upstream_happy_path"
        and:
            //device Info
            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
            def deviceDSN = "device_NO_1"
            def topic = "UP/" + deviceDSN
            def device = aclClientFactory.createMqtt5Client(deviceDSN).toAsync()

            //dnc Info
            def dnc = aclClientFactory.createMqtt5Client().toAsync()
            def dncSubscribeQoS = MqttQos.AT_MOST_ONCE
        and:
            def checkPoint = new AtomicInteger(0)
        when:
            connectAsDnc(dnc)


            subscribeMessage(dnc, subscribeUpTopicFilter, dncSubscribeQoS, { Mqtt5Publish pub ->
                checkPoint.addAndGet(1)
                printReceivedMessage(pub)
            })

            connectAsDevice(device)

            publishMessage(device, topic, devicePublishQoS, message)

            Thread.sleep(15000)

            disconnectAndClearSession(device)
            disconnectAndClearSession(dnc)
        then:
            checkPoint.get() == 1
    }

    /***
     * Scenario 3 : publish message
     */
//    def "Scenario 1 : publish message"() {
//        given:
//
//            //device Info
//            def devicePublishQoS = MqttQos.AT_LEAST_ONCE
//            def deviceDSN = "AC000000001"
//            def device = MqttClientHelper.createMqtt5Client(deviceDSN).toAsync()
//
//        and:
//            //message info
//            def publishTopic = "UP/"+ deviceDSN
//            def subscribeTopic = "DOWN/"+ deviceDSN
//            def messages = [
//                    "shared subscription message1"
//            ]
//        when:
//            connectAsDevice(device)
//
//            subscribeMessage(device, subscribeTopic, devicePublishQoS, {
//                MqttPublish msg -> printReceivedMessage(msg)
//            })
//
//            messages.each { message ->
//                publishMessage(device, publishTopic, devicePublishQoS, message)
//            }
//
//            while(true){
//                sleep(100)
//            }
//
//            disconnectAndClearSession(device)
//        then:
//            1 == 1
//    }


}
