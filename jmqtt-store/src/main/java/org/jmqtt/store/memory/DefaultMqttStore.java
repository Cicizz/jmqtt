package org.jmqtt.store.memory;

import org.jmqtt.store.*;

/**
 * 基于内存的mqtt数据存储
 */
public class DefaultMqttStore extends AbstractMqttStore {

    public DefaultMqttStore(){

    }

    @Override
    public void init(){
        this.flowMessageStore = new DefaultFlowMessageStore();
        this.willMessageStore = new DefaultWillMessageStore();
        this.retainMessageStore = new DefaultRetainMessageStore();
        this.offlineMessageStore = new DefaultOfflineMessageStore();
        this.subscriptionStore = new DefaultSubscriptionStore();
        this.sessionStore = new DefaultSessionStore();
    }

    @Override
    public void shutdown() {

    }

}
