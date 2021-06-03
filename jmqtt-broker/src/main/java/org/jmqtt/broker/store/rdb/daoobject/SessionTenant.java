
package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;

public class SessionTenant extends TenantBase implements Serializable {

    private static final long serialVersionUID = 12213131231231L;

    private Long id;

    private String clientId;

    private String state;

    private Long offlineTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(Long offlineTime) {
        this.offlineTime = offlineTime;
    }
}
