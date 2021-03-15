package com.jmqtt.mqtt.v3.acceptance.service.v1;

import com.jmqtt.mqtt.v3.acceptance.constant.JmqttRequestType;
import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;

public class HttpRequestFactory {

    public static HttpRequest gettestHttpRequest(JmqttRequestType requestType, MqttDeviceInfo deviceInfo){
        switch (requestType){
            case GET_DSNS:
                return new GetDsnsHttpRequest(deviceInfo);
            case PUT_OEM_INFO:
                return new PutOemInfoHttpRequest(deviceInfo);
            case GET_COMMANDS:
                return new GetCommandsHttpRequest(deviceInfo);
            case POST_DATAPOINT:
                return new PostDatapointHttpRequest(deviceInfo);
            default:
                throw new UnsupportedOperationException("Not Supported testHttpRequestType");

        }
    }

}
