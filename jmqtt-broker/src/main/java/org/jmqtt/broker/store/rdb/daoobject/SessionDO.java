
package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;

public class SessionDO implements Serializable {

    private static final long serialVersionUID = 12213131231231L;

    private long id;

    private String clientId;

    private String state;

    private long offlineTime;


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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(long offlineTime) {
        this.offlineTime = offlineTime;
    }
}
