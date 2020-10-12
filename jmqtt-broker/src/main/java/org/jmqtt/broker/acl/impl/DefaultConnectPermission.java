package org.jmqtt.broker.acl.impl;

import org.jmqtt.broker.acl.ConnectPermission;


public class DefaultConnectPermission implements ConnectPermission {


    @Override
    public boolean clientIdVerify(String clientId) {
        return true;
    }

    @Override
    public boolean onBlacklist(String remoteAddr, String clientId) {
        return false;
    }

    @Override
    public boolean authentication(String clientId, String userName, byte[] password) {
        return true;
    }

    @Override
    public boolean verifyHeartbeatTime(String clientId, int time) {
        return true;
    }
}
