package org.jmqtt.broker.processor;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.highperformance.InflowMessageHandler;
import org.jmqtt.broker.store.highperformance.OutflowMessageHandler;
import org.jmqtt.broker.store.highperformance.OutflowSecMessageHandler;

import java.util.Collection;

public abstract class HighPerformanceMessageHandler {

    // high performance message handle
    private InflowMessageHandler     inflowMessageHandler;
    private OutflowMessageHandler    outflowMessageHandler;
    private OutflowSecMessageHandler outflowSecMessageHandler;
    private boolean      highPerformance;
    private SessionStore sessionStore;

    public HighPerformanceMessageHandler(BrokerController brokerController){
        this.highPerformance = brokerController.getBrokerConfig().isHighPerformance();
        this.sessionStore = brokerController.getSessionStore();
        this.inflowMessageHandler = new InflowMessageHandler();
        this.outflowMessageHandler = new OutflowMessageHandler();
        this.outflowSecMessageHandler = new OutflowSecMessageHandler();
    }

    protected boolean cacheInflowMsg(String clientId, Message message) {
        if (highPerformance) {
            return this.inflowMessageHandler.cacheInflowMsg(clientId,message);
        }
        return this.sessionStore.cacheInflowMsg(clientId,message);
    }

    protected Message releaseInflowMsg(String clientId,int msgId){
        if (highPerformance) {
            return this.inflowMessageHandler.releaseInflowMsg(clientId,msgId);
        }
        return sessionStore.releaseInflowMsg(clientId,msgId);
    }

    protected Collection<Message> getAllInflowMsg(String clientId){
        if (highPerformance) {
            return this.inflowMessageHandler.getAllInflowMsg(clientId);
        }
        return sessionStore.getAllInflowMsg(clientId);
    }

    protected boolean cacheOutflowMsg(String clientId,Message message) {
        if (highPerformance) {
            return this.outflowMessageHandler.cacheOutflowMsg(clientId,message);
        }
        return this.sessionStore.cacheOutflowMsg(clientId,message);
    }

    protected Message releaseOutflowMsg(String clientId, int msgId){
        if (highPerformance) {
            return this.outflowMessageHandler.releaseOutflowMsg(clientId,msgId);
        }
        return this.sessionStore.releaseOutflowMsg(clientId,msgId);
    }

    protected Collection<Message> getAllOutflowMsg(String clientId){
        if (highPerformance) {
            return this.outflowMessageHandler.getAllOutflowMsg(clientId);
        }
        return this.sessionStore.getAllOutflowMsg(clientId);
    }

    protected boolean cacheOutflowSecMsgId(String clientId, int msgId) {
        if (highPerformance) {
            return this.outflowSecMessageHandler.cacheOutflowSecMsgId(clientId,msgId);
        }
        return this.sessionStore.cacheOutflowSecMsgId(clientId,msgId);
    }

    protected boolean releaseOutflowSecMsgId(String clientId, int msgId) {
        if (highPerformance) {
            return this.outflowSecMessageHandler.releaseOutflowSecMsgId(clientId,msgId);
        }
        return this.sessionStore.releaseOutflowSecMsgId(clientId,msgId);
    }

    protected Collection<Integer> getAllOutflowSecMsgId(String clientId) {
        if (highPerformance) {
            return this.outflowSecMessageHandler.getAllOutflowSecMsgId(clientId);
        }
        return this.sessionStore.getAllOutflowSecMsgId(clientId);
    }

}
