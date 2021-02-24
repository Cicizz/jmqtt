package com.jmqtt.mqtt.v5.acceptance.end2end


import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo
import com.jmqtt.mqtt.AbstractMqttV1Specification
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class V1EndToEndTest extends AbstractMqttV1Specification{

    @Value('${testData.testDevice1.dsn}')
    private String dsn;

    @Value('${testData.testDevice1.publicKey}')
    private String publicKey;

    @Value('${testData.testDevice1.oemId}')
    private String oemId;

    @Value('${testData.testDevice1.oemKey}')
    private String oemKey;

    @Value('${testData.testDevice1.oemModel}')
    private String oemModel;

    @Value('${testData.testDevice1.templateVersion}')
    private String templateVersion;


    MqttDeviceInfo deviceInfo;



    def setup(){
        deviceInfo = MqttDeviceInfo.builder()
                .dsn(dsn)
                .pubicKey(publicKey)
                .oemId(oemId)
                .oemModel(oemModel)
                .oemKey(oemKey)
                .templateVersion(templateVersion)
                .build()
    }

    def "scenario 1: connect and getDsns"(){
        given: "v1 device general actions"
            def v1Device = createV1Device(deviceInfo)
        when:
            v1Device.createMqttClient()
            v1Device.connectAndSubscribeTopic()
            sleep(10000)
            v1Device.getDsns()
            sleep(10000)
            v1Device.putOemInfo()
            sleep(15000)
            v1Device.postDatapoint("input", 10)
            sleep(10000)
            v1Device.getCommands()
            sleep(10000)
        then:
            notThrown(Exception)
    }

    def "scenario 2: Wait for notification"(){
        given: "v1 device receive notification"
            def v1Device = createV1Device(deviceInfo)
        when:
            v1Device.createMqttClient()
            v1Device.connectAndSubscribeTopic()
            sleep(10000)
            v1Device.waitForNotification(1, TimeUnit.MINUTES)
            //put notification with this file
            //curl -X POST -H 'Content-Type:application/xml' --data '<?xml version="1.0" encoding="UTF-8"?><request><cmd-name>notify</cmd-name><dsn>AC0050W000000248</dsn></request>'  http://127.0.0.1:8080/dnc/v1/devices
            sleep(10000)
        then:
            notThrown(Exception)
    }
}
