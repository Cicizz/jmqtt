package com.jmqtt.mqtt.v3.acceptance.model;

import com.jmqtt.mqtt.v3.acceptance.util.AuthenticationUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public class MqttDeviceInfo {

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
