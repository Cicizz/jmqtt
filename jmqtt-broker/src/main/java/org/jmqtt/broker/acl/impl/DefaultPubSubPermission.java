package org.jmqtt.broker.acl.impl;

import org.jmqtt.broker.acl.PubSubPermission;

public class DefaultPubSubPermission implements PubSubPermission {

    @Override
    public boolean publishVerfy(String clientId, String topic) {
        return true;
    }

    @Override
    public boolean subscribeVerfy(String clientId, String topic) {
        return true;
    }
}
