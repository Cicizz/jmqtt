package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;

import java.util.Map;

public class GetCommandsHttpRequest extends HttpRequest {

    public static final String urlPattern = "/devices/{}/commands.json";
    public static final String method = "GET";


    public GetCommandsHttpRequest(MqttDeviceInfo mqttDeviceInfo) {
        super(urlPattern, method, mqttDeviceInfo);
    }

    @Override
    public String getUrl() {
        return CommonUtils.formatString(urlPattern, mqttDeviceInfo.getDeviceId());
    }

    @Override
    public Map<String, String> getPayload() {
        return null;
    }
}
