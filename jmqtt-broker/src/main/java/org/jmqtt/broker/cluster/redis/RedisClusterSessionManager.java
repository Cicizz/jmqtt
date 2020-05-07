
package org.jmqtt.broker.cluster.redis;

import org.jmqtt.broker.cluster.ClusterSessionManager;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.common.model.Subscription;
import org.jmqtt.store.SessionStore;
import org.jmqtt.store.SubscriptionStore;

import java.util.Collection;

public class RedisClusterSessionManager extends ClusterSessionManager {

    private SessionStore      sessionStore;
    private  SubscriptionStore subscriptionStore;


    public RedisClusterSessionManager(SessionStore sessionStore,SubscriptionStore subscriptionStore) {
        this.sessionStore = sessionStore;
        this.subscriptionStore = subscriptionStore;
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

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}