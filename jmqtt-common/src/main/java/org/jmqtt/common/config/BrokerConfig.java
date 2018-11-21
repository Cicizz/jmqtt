package org.jmqtt.common.config;

public class BrokerConfig {

    private String jmqttHome = System.getProperty("jmqttHome",System.getenv("JMQTT_HOME"));

    private String version = "1.0.0";

    private boolean anonymousEnable = true;


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

    public boolean isAnonymousEnable() {
        return anonymousEnable;
    }

    public void setAnonymousEnable(boolean anonymousEnable) {
        this.anonymousEnable = anonymousEnable;
    }
}
