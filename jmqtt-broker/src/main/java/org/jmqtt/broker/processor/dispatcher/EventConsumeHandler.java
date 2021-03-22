package org.jmqtt.broker.processor.dispatcher;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 集群事件处理
 */
public class EventConsumeHandler {

    private static final Logger log = JmqttLogger.eventLog;

    private InnerMessageDispatcher innerMessageDispatcher;
    private ClusterEventHandler    clusterEventHandler;
    private AtomicBoolean          pollStoped = new AtomicBoolean(false);
    private int                    maxPollNum;
    private int                    pollWaitInterval;
    private String                 currentIp;
    private SessionStore           sessionStore;
    private SubscriptionMatcher    subscriptionMatcher;

    public EventConsumeHandler(BrokerController brokerController) {
        this.innerMessageDispatcher = brokerController.getInnerMessageDispatcher();
        this.clusterEventHandler = brokerController.getClusterEventHandler();
        this.maxPollNum = brokerController.getBrokerConfig().getMaxPollEventNum();
        this.pollWaitInterval = brokerController.getBrokerConfig().getPollWaitInterval();
        this.currentIp = brokerController.getCurrentIp();
        this.sessionStore = brokerController.getSessionStore();
        this.subscriptionMatcher = brokerController.getSubscriptionMatcher();
    }

    // 集群方式1: consume event from cluster
    public void consumeEvent(Event event) {
        switch (event.getEventCode()) {
            case 1:
                clearClientSession(event);
                break;
            case 2:
                dispatcherMessage(event);
                break;
            case 3:
                dispatcherMessage(event);
                break;
            default:
                LogUtil.warn(log, "[EventConsumeHandler] consume event is not supported,event:{}", event);
        }
    }

    // 集群方式2: poll event from cluster
    private List<Event> pollEvent() {
        return clusterEventHandler.pollEvent(maxPollNum);
    }

    public void start() {
        clusterEventHandler.setEventConsumeHandler(this);

        new Thread(() -> {
            while (!pollStoped.get()) {
                try {
                    List<Event> eventList = pollEvent();
                    if (!MixAll.isEmpty(eventList)) {
                        for (Event event : eventList) {
                            consumeEvent(event);
                        }
                    }
                    if (MixAll.isEmpty(eventList) || eventList.size() < 5) {
                        Thread.sleep(pollWaitInterval);
                    }
                } catch (Exception e) {
                    LogUtil.warn(log, "Poll event from cluster error.", e);
                }
            }
        }).start();
    }

    public void shutdown() {

    }

    void dispatcherMessage(Event event) {
        Message message = JSONObject.parseObject(event.getBody(), Message.class);
        this.innerMessageDispatcher.appendMessage(message);
    }

    void clearClientSession(Event event) {
        // if event from current node, ignore the event
        if (currentIp.equals(event.getFromIp())) {
            LogUtil.debug(log, "Event from current node,ignore the event,fromIp:{}", event.getFromIp());
            return;
        }
        String clientId = event.getBody();
        if (!ConnectManager.getInstance().containClient(clientId)) {
            return;
        }
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        clientSession.getCtx().close();
        Set<Subscription> subscriptionSet = sessionStore.getSubscriptions(clientId);
        if (!MixAll.isEmpty(subscriptionSet)) {
            subscriptionSet.forEach(item -> {
                subscriptionMatcher.unSubscribe(item.getTopic(), clientId);
            });
        }
    }

}
