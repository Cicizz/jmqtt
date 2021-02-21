package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;

import java.util.Map;

public class GetDsnsHttpRequest extends HttpRequest {

    public static final String urlPattern = "/dsns/{}.json";
    public static final String method = "GET";


    public GetDsnsHttpRequest(MqttDeviceInfo mqttDeviceInfo) {
        super(urlPattern, method, mqttDeviceInfo);
    }

    @Override
    public String getUrl() {
        return CommonUtils.formatString(urlPattern, mqttDeviceInfo.getDsn());
    }

    @Override
    public Map<String, String> getHeaders() {
        Map header = super.getHeaders();
        header.remove("x-test-auth-key");
        header.put("x-test-client-auth", "test1.0 " + super.mqttDeviceInfo.generateCipher());
        return header;
    }

    @Override
    public Map<String, String> getPayload() {
        return null;
    }
}
