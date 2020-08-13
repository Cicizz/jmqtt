
package org.jmqtt.broker.cluster;

import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.common.model.Subscription;
import org.jmqtt.store.SessionStore;
import org.jmqtt.store.SubscriptionStore;

import java.util.Collection;

public class DefaultClusterSessionManager extends ClusterSessionManager {

    private SessionStore sessionStore;
    private SubscriptionStore subscriptionStore;


    public DefaultClusterSessionManager(SessionStore sessionStore,SubscriptionStore subscriptionStore) {
        this.sessionStore = sessionStore;
        this.subscriptionStore = subscriptionStore;
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    protected CommandReqOrResp queryLastState(CommandReqOrResp request) {
        String clientId = (String) request.getBody();
        Object lastState = sessionStore.getLastSession(clientId);
        CommandReqOrResp response = new CommandReqOrResp(request.getCommandCode(),lastState);
        return response;
    }

    @Override
    protected CommandReqOrResp getSubscriptions(CommandReqOrResp request) {
        String clientId = (String) request.getBody();
        Collection<Subscription> subscriptionCollection = subscriptionStore.getSubscriptions(clientId);
        CommandReqOrResp response = new CommandReqOrResp(request.getCommandCode(),subscriptionCollection);
        return response;
    }
}