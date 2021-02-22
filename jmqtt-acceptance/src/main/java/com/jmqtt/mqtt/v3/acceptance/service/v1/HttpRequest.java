package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public abstract class HttpRequest {

    Logger log = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(String urlPattern, String method, MqttDeviceInfo mqttDeviceInfo) {
        this.urlPattern = urlPattern;
        this.method = method;
        this.mqttDeviceInfo = mqttDeviceInfo;
        reqId = ThreadLocalRandom.current().nextInt();
    }

    private Integer reqId;
    //initial fields
    protected String urlPattern;
    protected String method ;

    protected MqttDeviceInfo mqttDeviceInfo;

    //generate
    public abstract String getUrl();
    public abstract Map<String, String> getPayload();

    //default generate auth token header
    public Map<String, String> getHeaders(){
        Map map = new HashMap<String, String>();
        map.put("x-test-auth-key", "test1.0 " + mqttDeviceInfo.getAuthToken());
        map.put("Content-Type", "application/json");
        return map;
    }

    public Integer getReqId() {
        return reqId;
    }

    public String getJSONRequest(){

        Map map = new HashMap<>();
        map.put("req_id",reqId);
        map.put("method", this.method);
        map.put("url", getUrl());
        map.put("header", getHeaders());
        if(!"GET".equalsIgnoreCase(this.method)){
            map.put("payload", getPayload() );
        }
        String jsonRequest = JSONUtil.toJson(map);
        log.debug("jsonRequest : {}", jsonRequest);
        return jsonRequest;
    }


}
