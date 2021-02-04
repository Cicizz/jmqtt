package org.jmqtt.broker.processor.dispatcher.rdb;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.store.rdb.AbstractDBStore;
import org.jmqtt.broker.store.rdb.daoobject.EventDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RDBClusterEventHandler extends AbstractDBStore implements ClusterEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.EVENT);

    private static final AtomicLong offset = new AtomicLong();

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
        Long maxId = getMapper(eventMapperClass).getMaxOffset();
        if (maxId == null) {
            log.error("RDBClusterEventHandler start error,event db max id is null");
            System.exit(-1);
        } else {
            offset.set(maxId);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public boolean sendEvent(Event event) {
        EventDO eventDO = new EventDO();
        eventDO.setJmqttIp(MixAll.getLocalIp());
        eventDO.setContent(event.getBody());
        eventDO.setEventCode(event.getEventCode());
        eventDO.setGmtCreate(event.getSendTime());
        Long id = getMapper(eventMapperClass).sendEvent(eventDO);
        return id != null;
    }

    @Override
    public void setEventConsumeHandler(EventConsumeHandler eventConsumeHandler) {
        // db not supported, do nothing
    }

    @Override
    public List<Event> pollEvent(int maxPollNum) {
        // offset: min -> max
        long currentOffset = offset.get();
        List<EventDO> eventDOList = getMapper(eventMapperClass).consumeEvent(currentOffset,maxPollNum);
        if (eventDOList == null || eventDOList.size() == 0) {
            return Collections.emptyList();
        }
        List<Event> events = new ArrayList<>();
        for (EventDO eventDO : eventDOList) {
            Event event = new Event(eventDO.getEventCode(),eventDO.getContent(),eventDO.getGmtCreate());
            events.add(event);
        }

        // reset offset
        EventDO eventDO = eventDOList.get(eventDOList.size()-1);
        if (!offset.compareAndSet(currentOffset,eventDO.getId())) {
            log.warn("[RDBClusterEventHandler] pollEvent offset is wrong,expectOffset:{},currentOffset:{},maxOffset:{}",
                    offset.get(),currentOffset,eventDO.getId());
            offset.set(eventDO.getId());
        }
        return events;
    }
}
