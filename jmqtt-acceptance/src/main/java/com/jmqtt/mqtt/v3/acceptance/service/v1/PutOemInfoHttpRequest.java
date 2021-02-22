package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;

import java.util.HashMap;
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
        HashMap<String, String> payLoad = new HashMap<>();
        payLoad.put("oem", super.mqttDeviceInfo.getOemId());
        payLoad.put("oem_key", super.mqttDeviceInfo.getOemSecret());
        payLoad.put("oem_model", super.mqttDeviceInfo.getOemModel());
        payLoad.put("template_version", super.mqttDeviceInfo.getTemplateVersion());
        return payLoad;
    }
}
