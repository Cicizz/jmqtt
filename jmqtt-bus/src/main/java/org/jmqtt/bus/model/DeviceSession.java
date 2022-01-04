
package org.jmqtt.bus.model;

import org.jmqtt.bus.enums.DeviceOnlineStateEnum;
import org.jmqtt.bus.enums.TransportProtocolEnum;

import java.util.Date;
import java.util.Map;

/**
 * session
 */
public class DeviceSession {

    private String clientId;

    private TransportProtocolEnum transportProtocol;

    private String clientIp;

    private String serverIp;

    private DeviceOnlineStateEnum online;

    private Date onlineTime;

    private Date lastOfflineTime;

    private Map<String,Object> properties;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public TransportProtocolEnum getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(TransportProtocolEnum transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public DeviceOnlineStateEnum getOnline() {
        return online;
    }

    public void setOnline(DeviceOnlineStateEnum online) {
        this.online = online;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Date onlineTime) {
        this.onlineTime = onlineTime;
    }

    public Date getLastOfflineTime() {
        return lastOfflineTime;
    }

    public void setLastOfflineTime(Date lastOfflineTime) {
        this.lastOfflineTime = lastOfflineTime;
    }
}
