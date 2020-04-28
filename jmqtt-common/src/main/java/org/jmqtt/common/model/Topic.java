package org.jmqtt.common.model;

public class Topic {

    private String topicName;
    private int qos;

    public Topic(String topicName, int qos) {
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
