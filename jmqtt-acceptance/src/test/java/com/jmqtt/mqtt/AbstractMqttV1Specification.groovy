package com.jmqtt.mqtt


import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo
import com.jmqtt.mqtt.v3.acceptance.model.MqttServer
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientFactory
import com.jmqtt.mqtt.v3.acceptance.util.MqttV1ClientFactory
import com.jmqtt.mqtt.v3.acceptance.device.MqttV1Device
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AbstractMqttV1Specification extends AbstractMqtt5Specification{

    private static final Logger logger = LoggerFactory.getLogger(AbstractMqttV1Specification.class);

    @Autowired
    @Qualifier("testMqttServer")
    protected MqttServer testServer;

    protected MqttClientFactory v1ClientFactory;

    def setup(){
        v1ClientFactory = new MqttV1ClientFactory(testServer)
    }

    def createV1Device(MqttDeviceInfo mqttDeviceInfo){
        return new MqttV1Device(v1ClientFactory, mqttDeviceInfo)
    }


}
