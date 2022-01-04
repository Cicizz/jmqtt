package org.jmqtt.mqtt.model;

public class MqttTopic {

    private String topicName;
    private int    qos;

    public MqttTopic(String topicName, int qos) {
        this.topicName = topicName;
        this.qos = qos;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }
}
