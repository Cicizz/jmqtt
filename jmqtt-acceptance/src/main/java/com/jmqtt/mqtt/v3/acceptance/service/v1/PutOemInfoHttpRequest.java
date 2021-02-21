package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;

import java.util.Map;

public class PutOemInfoHttpRequest extends HttpRequest {

    public static final String urlPattern = "/devices/{}/oem_info.json";
    public static final String method = "PUT";


    public PutOemInfoHttpRequest(MqttDeviceInfo mqttDeviceInfo) {
        super(urlPattern, method, mqttDeviceInfo);
    }

    @Override
    public String getUrl() {
        return CommonUtils.formatString(urlPattern, mqttDeviceInfo.getDeviceId());
    }


    @Override
    public Map<String, String> getPayload() {
        return Map.of(
            "oem", super.mqttDeviceInfo.getOemId()
            ,"oem_key", super.mqttDeviceInfo.getOemSecret()
            ,"oem_model", super.mqttDeviceInfo.getOemModel()
            ,"template_version", super.mqttDeviceInfo.getTemplateVersion()

        );
    }
}
