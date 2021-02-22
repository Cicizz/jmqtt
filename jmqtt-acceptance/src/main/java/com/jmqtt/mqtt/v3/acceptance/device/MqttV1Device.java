package com.jmqtt.mqtt.v3.acceptance.device;

import com.jmqtt.mqtt.v3.acceptance.constant.JmqttRequestType;
import com.jmqtt.mqtt.v3.acceptance.constant.MqttV1ClientConstant;
import com.jmqtt.mqtt.v3.acceptance.model.MqttDeviceInfo;
import com.jmqtt.mqtt.v3.acceptance.service.v1.HttpRequest;
import com.jmqtt.mqtt.v3.acceptance.service.v1.HttpRequestFactory;
import com.jmqtt.mqtt.v3.acceptance.service.v1.PostDatapointHttpRequest;
import com.jmqtt.mqtt.v3.acceptance.util.CommonUtils;
import com.jmqtt.mqtt.v3.acceptance.util.JSONUtil;
import com.jmqtt.mqtt.v3.acceptance.util.MqttClientUtils;
import com.jmqtt.mqtt.v3.acceptance.util.MqttV1ClientFactory;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MqttV1Device {

    Logger log = LoggerFactory.getLogger(MqttV1Device.class);

    private MqttDeviceInfo mqttDeviceInfo;

    private MqttV1ClientFactory mqttV1ClientFactory;

    private Mqtt5BlockingClient mqtt5Client;

    private Map<String, Consumer<Map>> responseActionMap = new HashMap<>();

    private LinkedBlockingQueue<Map> waitingQueue = new LinkedBlockingQueue<>();


    public MqttV1Device(MqttV1ClientFactory mqttV1ClientFactory, MqttDeviceInfo deviceInfo){
        this.mqttV1ClientFactory = mqttV1ClientFactory;
        this.mqttDeviceInfo = deviceInfo;
        responseActionMap.put("auth", this::saveDeviceIdAndAuthToken);
        responseActionMap.put("notify", this::handleNotify);
    }

    public void createMqttClient(){
        mqtt5Client = mqttV1ClientFactory.createMqttV1Device(mqttDeviceInfo.getDsn(), mqttDeviceInfo.generateCipher()).toBlocking();
    }

    public void connectAndSubscribeTopic() {
        log.info("connecting to broker from client :broker host ={}, port={},device dsn = {}"
            ,mqttV1ClientFactory.getMqttServer().getHost()
            ,mqttV1ClientFactory.getMqttServer().getPort()
            , mqttDeviceInfo.getDsn());
        mqtt5Client.toBlocking().connect();
        log.info("connected to broker from client : dsn = {}", mqttDeviceInfo.getDsn());
        MqttClientUtils.subscribeMessage(mqtt5Client, getSubscribeTopicFilter(), MqttQos.fromCode(1), this::handleDownstreamMessage);
    }

    public void getDsns(){
        HttpRequest getDsnsHttpRequest = HttpRequestFactory.gettestHttpRequest(JmqttRequestType.GET_DSNS, mqttDeviceInfo);
        responseActionMap.put(getDsnsHttpRequest.getReqId()+"", this::saveDeviceIdAndAuthToken);
        publishMessage(getDsnsHttpRequest.getJSONRequest());
    }

    public void postDatapoint(String name, Object value){
        postDatapoint(name, value, null);
    }

    public void postDatapoint(String name, Object value, Map metadata){
        PostDatapointHttpRequest postDatapointHttpRequesst = (PostDatapointHttpRequest) HttpRequestFactory.gettestHttpRequest(JmqttRequestType.POST_DATAPOINT, mqttDeviceInfo);
        postDatapointHttpRequesst.setName(name);
        postDatapointHttpRequesst.setValue(value);
        postDatapointHttpRequesst.setMetadata(metadata);
        responseActionMap.put(postDatapointHttpRequesst.getReqId()+"", (response) ->{log.info("Post Datapoint Success");});
        publishMessage(postDatapointHttpRequesst.getJSONRequest());
    }


    public void putOemInfo(){
        if(mqttDeviceInfo.getOemSecret() == null){
            mqttDeviceInfo.createAndSetOemSecret();
        }
        HttpRequest putOemInfoRequest = HttpRequestFactory.gettestHttpRequest(JmqttRequestType.PUT_OEM_INFO, mqttDeviceInfo);
        responseActionMap.put(putOemInfoRequest.getReqId()+"", (response) ->{log.info("Put OemInfo Success");});
        publishMessage(putOemInfoRequest.getJSONRequest());
    }

    public void getCommands() {
        log.info("Call get commands: dsn={}", mqttDeviceInfo.getDsn());
        HttpRequest getCommandsInfoRequest = HttpRequestFactory.gettestHttpRequest(JmqttRequestType.GET_COMMANDS, mqttDeviceInfo);
        responseActionMap.put(getCommandsInfoRequest.getReqId()+"", (response) ->{log.info("GET Commands Success");});
        publishMessage(getCommandsInfoRequest.getJSONRequest());

    }

    public void waitForNotification(Long time, TimeUnit timeUnit) throws InterruptedException {
        log.info("Waiting for notification : dsn={}, time={} {}", mqttDeviceInfo.getDsn(), time, timeUnit);
        waitingQueue.poll(time, timeUnit);
    }


    private void handleDownstreamMessage(Mqtt5Publish downstreamMessage){
        final String msg =  new String(downstreamMessage.getPayloadAsBytes());
        log.info("receive downstream message : topic = {}, message = {}", downstreamMessage.getTopic(), msg);
        final Map responseMap = JSONUtil.fromJson(msg, Map.class);

        if(responseMap.containsKey("req_id")){
            final Integer id = (Integer) responseMap.get("req_id");


            if( (isAuthResponse(id))){
                if(validateStatusSuccess(responseMap)){
                    responseActionMap.get("auth").accept(responseMap);
                }
            }else if( isNotifyRequest(id)){
                responseActionMap.get("notify").accept(responseMap);
            }else{
                throw new UnsupportedOperationException("This request id is not legal : "+ Integer.toBinaryString(id));
            }

        }else if(responseMap.containsKey("resp_id")){
            final Integer id = (Integer) responseMap.get("resp_id");
            if(validateStatusSuccess(responseMap)){
                if(responseActionMap.containsKey(id.toString())){
                    responseActionMap.get(id.toString()).accept(responseMap);
                }else{
                    log.error("cannot find the request action from resp_id : ", id);
                }
            }
        }
    }



    private boolean validateStatusSuccess(Map responseMap) {
         final Integer status = (Integer)responseMap.get("status");
        if(status == 200 || status == 201){
            return true;
        }else{
            log.warn("Request failed : status={}, response={}",status, responseMap);
            return false;
        }
    }

    private boolean isAuthResponse(Integer id){
        return (MqttV1ClientConstant.IdPrefix.GET_DSN.getPrefix() & id)  == MqttV1ClientConstant.IdPrefix.GET_DSN.getPrefix();
    }

    private boolean isNotifyRequest(Integer id){
        return (MqttV1ClientConstant.IdPrefix.NOTIFY.getPrefix() & id)  == MqttV1ClientConstant.IdPrefix.NOTIFY.getPrefix();
    }

    private void saveDeviceIdAndAuthToken(Map responseMap) {
        String authToken = (String)((Map)responseMap.get("header")).get("x-test-auth-key");
        mqttDeviceInfo.setAuthToken(authToken);
        Integer deviceKey = (Integer) ((Map)((Map)responseMap.get("payload")).get("device")).get("key");
        mqttDeviceInfo.setDeviceId(deviceKey);
        log.info("saveDeviceIdAndAuthToken : deviceId={}, authToken={}",deviceKey, authToken);
    }

    private void handleNotify(Map map) {
        log.info("Receive notification: dsn={}",mqttDeviceInfo.getDsn());
        waitingQueue.offer(map);
        this.getCommands();
    }

    private void publishMessage(String message){
        MqttClientUtils.publishMessage(mqtt5Client, getPublishTopic(), MqttQos.fromCode(1), message, (long)2000);
    }



    private String getPublishTopic() {
        return CommonUtils.formatString(MqttV1ClientConstant.INSTANCE.getPublishTopic(), mqttDeviceInfo.getDsn());
    }

    private String getSubscribeTopicFilter(){
        return CommonUtils.formatString(MqttV1ClientConstant.INSTANCE.getSubscribeTopicFilter(), mqttDeviceInfo.getDsn());
    }
}
