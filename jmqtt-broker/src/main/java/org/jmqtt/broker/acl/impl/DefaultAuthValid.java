package org.jmqtt.broker.acl.impl;

import org.jmqtt.broker.acl.AuthValid;


public class DefaultAuthValid implements AuthValid {

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

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

    @Override
    public boolean publishVerify(String clientId, String topic) {
        return true;
    }

    @Override
    public boolean subscribeVerify(String clientId, String topic) {
        return true;
    }
}
