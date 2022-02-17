package org.jmqtt.bus.subscription;

import org.jmqtt.bus.subscription.model.Subscription;
import org.jmqtt.support.log.JmqttLogger;
import org.slf4j.Logger;

import java.util.Set;

public class CTrieSubscriptionMatcher implements SubscriptionMatcher {

    private static final Logger log = JmqttLogger.busLog;

    private CTrie ctrie;

    public CTrieSubscriptionMatcher(){
        this.ctrie = new CTrie();
    }

    @Override
    public Set<Subscription> match(String topicFilter) {
        Topic topic = new Topic(topicFilter);
        return ctrie.recursiveMatch(topic);
    }


    @Override
    public boolean subscribe(Subscription newSubscription) {
        ctrie.addToTree(newSubscription);
        return true;
    }


    @Override
    public boolean unSubscribe(String topicFilter, String clientID) {
        ctrie.removeFromTree(new Topic(topicFilter), clientID);
        return true;
    }

    @Override
    public int size() {
        return ctrie.size();
    }

    @Override
    public String dumpTree() {
        return ctrie.dumpTree();
    }
}
