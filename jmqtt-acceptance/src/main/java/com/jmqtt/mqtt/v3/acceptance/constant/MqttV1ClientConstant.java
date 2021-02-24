package com.jmqtt.mqtt.v3.acceptance.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
@ConfigurationProperties(prefix="device.v1")
public class MqttV1ClientConstant {

    public static MqttV1ClientConstant INSTANCE;

    private String proofUserName;
    private String publishTopic;
    private String subscribeTopicFilter;

    @PostConstruct
    public void init(){
        INSTANCE = this;
    }


    private static int ID_PREFIX_OFFSET = 24;
    public static enum IdPrefix {
        GET_DSN(IdPrefix.INT_GET_DSN << ID_PREFIX_OFFSET),

        NOTIFY(IdPrefix.INT_NOTIFY << ID_PREFIX_OFFSET),

        V3_IFP(IdPrefix.INT_V3_IFP << ID_PREFIX_OFFSET),

        V3_OB(IdPrefix.INT_V3_OB << ID_PREFIX_OFFSET),

        V3_PRF(IdPrefix.INT_V3_PRF << ID_PREFIX_OFFSET);

        private final Integer prefix;

        private static final int INT_GET_DSN = 0x01;

        private static final int INT_NOTIFY = 0x02;

        private static final int INT_V3_IFP = 0x03;

        private static final int INT_V3_OB = 0x04;

        private static final int INT_V3_PRF = 0x05;

        IdPrefix(final Integer prefix) {
            this.prefix = prefix;
        }

        public Integer getPrefix() {
            return prefix;
        }
    }

    public String getProofUserName() {
        return proofUserName;
    }

    public void setProofUserName(String proofUserName) {
        this.proofUserName = proofUserName;
    }

    public String getPublishTopic() {
        return publishTopic;
    }

    public void setPublishTopic(String publishTopic) {
        this.publishTopic = publishTopic;
    }

    public String getSubscribeTopicFilter() {
        return subscribeTopicFilter;
    }

    public void setSubscribeTopicFilter(String subscribeTopicFilter) {
        this.subscribeTopicFilter = subscribeTopicFilter;
    }

    public static int getIdPrefixOffset() {
        return ID_PREFIX_OFFSET;
    }

    public static void setIdPrefixOffset(int idPrefixOffset) {
        ID_PREFIX_OFFSET = idPrefixOffset;
    }
}
