/**
 * Alipay.com Inc. Copyright (c) 2004-2021 All Rights Reserved.
 */
package org.jmqtt.broker.store.memory;

import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;

import java.util.Collection;
import java.util.Set;

/**
 * 内存-会话信息存储
 */
public class DefaultSessionStore implements SessionStore {
    @Override
    public SessionState getSession(String clientId) {
        return null;
    }

    @Override
    public boolean storeSession(String clientId, SessionState sessionState, boolean notifyClearOtherSession) {
        return false;
    }

    @Override
    public void clearSession(String clientId) {

    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        return false;
    }

    @Override
    public boolean delSubscription(String clientId, String topic) {
        return false;
    }

    @Override
    public Set<Subscription> getSubscriptions(String clientId) {
        return null;
    }

    @Override
    public boolean cacheInflowMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        return null;
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public boolean containOutflowMsg(String clientId, int msgId) {
        return false;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        return null;
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        return null;
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        return false;
    }
}
