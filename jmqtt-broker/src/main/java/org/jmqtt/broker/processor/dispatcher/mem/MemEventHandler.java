package org.jmqtt.broker.processor.dispatcher.mem;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.store.mem.AbstractMemStore;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于内存的事件队列
 **/
public class MemEventHandler extends AbstractMemStore implements ClusterEventHandler {
    private static final Logger log = JmqttLogger.eventLog;
    private ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<Event>();
    private AtomicLong size = new AtomicLong(0);

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public boolean sendEvent(Event event) {
        long maxCap = 100000;
        if (size.getAndAdd(1) > maxCap){
            LogUtil.warn(log,"event queue capacity exceeds {}", maxCap);
        }
        return events.offerLast(event);
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {

    }

    // TODO 这里考虑能否批量拉取，降低事件复杂度
    @Override
    public List<Event> pollEvent(int maxPollNum) {
        List<Event> e = new LinkedList<Event>();
        for (int i = 0; i < maxPollNum; i++) {
            Event e1 = events.pollFirst();
            if(e1 == null){
                break;
            }
            e.add(e1);
        }
        return e;
    }
}
