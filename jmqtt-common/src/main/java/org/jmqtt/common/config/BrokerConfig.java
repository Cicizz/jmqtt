package org.jmqtt.common.config;

public class BrokerConfig {

    private String jmqttHome = System.getProperty("jmqttHome",System.getenv("JMQTT_HOME"));

    private String version = "1.0.0";


    public String getJmqttHome() {
        return jmqttHome;
    }

    public void setJmqttHome(String jmqttHome) {
        this.jmqttHome = jmqttHome;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
