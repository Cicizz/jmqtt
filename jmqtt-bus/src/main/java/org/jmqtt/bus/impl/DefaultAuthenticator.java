package org.jmqtt.bus.impl;

import org.jmqtt.bus.Authenticator;


public class DefaultAuthenticator implements Authenticator {

    @Override
    public boolean login(String clientId, String userName, byte[] password) {
        return true;
    }

    @Override
    public boolean onBlackList(String clientId, String remoteIpAddress) {
        return false;
    }

    @Override
    public boolean clientIdVerify(String clientId) {
        return true;
    }

    @Override
    public boolean subscribeVerify(String clientId, String topic) {
        return true;
    }
}
