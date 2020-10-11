package org.jmqtt.broker.acl.impl;

import org.jmqtt.broker.acl.PubSubPermission;

public class DefaultPubSubPermission implements PubSubPermission {

    @Override
    public boolean publishVerify(String clientId, String topic) {
        return true;
    }

    @Override
    public boolean subscribeVerify(String clientId, String topic) {
        return true;
    }
}
