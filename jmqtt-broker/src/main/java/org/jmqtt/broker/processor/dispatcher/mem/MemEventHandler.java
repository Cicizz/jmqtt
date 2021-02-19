package org.jmqtt.broker.processor.dispatcher.mem;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.store.mem.AbstractMemStore;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * TODO 基于内存的事件队列，最好控制下队列大小，在超过100000时，打warn日志，方便排查问题
 * @program: jmqtt
 * @description:
 * @author: Mr.Liu
 * @create: 2021-02-14 21:59
 **/
public class MemEventHandler extends AbstractMemStore implements ClusterEventHandler {
    private ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<Event>();
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
