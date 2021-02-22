package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PostDatapointHttpRequest extends HttpRequest {

    public static final String urlPattern = "/devices/{}/properties/{}/datapoints.json";
    public static final String method = "POST";

    private String name;
    private Object value;
    private Map metadata;



    public PostDatapointHttpRequest(MqttDeviceInfo mqttDeviceInfo) {
        super(urlPattern, method, mqttDeviceInfo);
    }

    @Override
    public String getUrl() {
        return CommonUtils.formatString(urlPattern, mqttDeviceInfo.getDeviceId(), name);
    }


    @Override
    public Map getPayload() {

        final Map datapoint = new HashMap();
        datapoint.put("value", value);
        if(metadata == null){
            datapoint.put("metadata", metadata);
        }
        return Collections.singletonMap("datapoint", datapoint);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }
}
