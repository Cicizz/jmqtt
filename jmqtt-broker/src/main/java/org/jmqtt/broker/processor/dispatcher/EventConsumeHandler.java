package org.jmqtt.broker.processor.dispatcher;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 集群事件处理
 */
public class EventConsumeHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.EVENT);

    private InnerMessageDispatcher innerMessageDispatcher;
    private ClusterEventHandler    clusterEventHandler;
    private AtomicBoolean          pollStoped = new AtomicBoolean(false);
    private int maxPollNum;
    private int pollWaitInterval;

    public EventConsumeHandler(BrokerController brokerController) {
        this.innerMessageDispatcher = brokerController.getInnerMessageDispatcher();
        this.clusterEventHandler = brokerController.getClusterEventHandler();
        this.maxPollNum = brokerController.getBrokerConfig().getMaxPollEventNum();
        this.pollWaitInterval = brokerController.getBrokerConfig().getPollWaitInterval();
    }

    // 集群方式1: consume event from cluster
    public void consumeEvent(Event event){
        switch (event.getEventCode()){
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
                log.warn("[EventConsumeHandler] consume event is not supported,event:{}",event);
        }
    }

    // 集群方式2: poll event from cluster
    private List<Event> pollEvent(){
        return clusterEventHandler.pollEvent(maxPollNum);
    }


    public void start(){
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
                    log.warn("Poll event from cluster error.",e);
                }
            }
        }).start();
    }


    public void shutdown(){

    }


    void dispatcherMessage(Event event) {
        Message message = JSONObject.parseObject(event.getBody(),Message.class);
        this.innerMessageDispatcher.appendMessage(message);
    }

    void clearClientSession(Event event){
        String clientId = event.getBody();
        if (ConnectManager.getInstance().containClient(clientId)){
            return;
        }
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        clientSession.getCtx().close();
    }

}
