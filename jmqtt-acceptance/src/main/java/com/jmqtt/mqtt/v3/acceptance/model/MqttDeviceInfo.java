package com.jmqtt.mqtt.v3.acceptance.model;

import com.jmqtt.mqtt.v3.acceptance.util.AuthenticationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttDeviceInfo {

    Logger log = LoggerFactory.getLogger(MqttDeviceInfo.class);

    private String dsn;

    private String pubicKey;

    private String oemId;

    private String oemKey;

    private String oemModel;



    private String templateVersion;


    //from cloud
    private String authToken;
    private Integer deviceId;

    //generated
    private String authProof;
    private String oemSecret;

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public String getDsn() {
        return dsn;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public String getPubicKey() {
        return pubicKey;
    }

    public void setPubicKey(String pubicKey) {
        this.pubicKey = pubicKey;
    }

    public String getOemId() {
        return oemId;
    }

    public void setOemId(String oemId) {
        this.oemId = oemId;
    }

    public String getOemKey() {
        return oemKey;
    }

    public void setOemKey(String oemKey) {
        this.oemKey = oemKey;
    }

    public String getOemModel() {
        return oemModel;
    }

    public void setOemModel(String oemModel) {
        this.oemModel = oemModel;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getAuthProof() {
        return authProof;
    }

    public void setAuthProof(String authProof) {
        this.authProof = authProof;
    }

    public String getOemSecret() {
        return oemSecret;
    }

    public void setOemSecret(String oemSecret) {
        this.oemSecret = oemSecret;
    }

    public static Builder builder(){
        return new Builder();
    }

    public String generateCipher(){
        try {
            return AuthenticationUtil.getProof(dsn, pubicKey);
        } catch (Exception e) {
            log.error("generate Proof failed : dsn = {}", dsn, e);
            throw new RuntimeException(e);
        }
    }

    public void createAndSetOemSecret(){
        try {
            if(hasOemInfo()){
                this.oemSecret =  AuthenticationUtil.getOemSecret(pubicKey, oemKey, oemId, oemModel);
                log.debug("set OEM secret : dsn = {}, oemSecret = {}", dsn, oemSecret);
            }else{
                log.warn("Do not have OEM info : dsn = {}", dsn);
            }
        } catch (Exception e) {
            log.error("get OEM secret failed : dsn = {}", dsn, e);
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        public Builder() {
            this.deviceInfo = new MqttDeviceInfo();
        }

        private MqttDeviceInfo deviceInfo;



        public MqttDeviceInfo build(){
            return deviceInfo;
        }

        public Builder dsn(String dsn){
            deviceInfo.setDsn(dsn);
            return this;
        }

        public Builder pubicKey(String pubicKey){
            deviceInfo.setPubicKey(pubicKey);
            return this;
        }

        public Builder oemId(String oemId){
            deviceInfo.setOemId(oemId);
            return this;
        }

        public Builder oemKey(String oemKey){
            deviceInfo.setOemKey(oemKey);
            return this;
        }

        public Builder oemModel(String oemModel){
            deviceInfo.setOemModel(oemModel);
            return this;
        }

        public Builder authToken(String authToken){
            deviceInfo.setAuthToken(authToken);
            return this;
        }

        public Builder templateVersion(String templateVersion){
            deviceInfo.setTemplateVersion(templateVersion);
            return this;
        }


    }


    public boolean hasOemInfo(){
        return StringUtils.isNotBlank(oemId)
            && StringUtils.isNotBlank(oemKey)
            && StringUtils.isNotBlank(oemModel)
            && StringUtils.isNotBlank(pubicKey);
    }

}
