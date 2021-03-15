package org.jmqtt.broker.processor.dispatcher.rdb;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.event.Event;
import org.jmqtt.broker.store.rdb.AbstractDBStore;
import org.jmqtt.broker.store.rdb.DBCallback;
import org.jmqtt.broker.store.rdb.daoobject.EventDO;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RDBClusterEventHandler extends AbstractDBStore implements ClusterEventHandler {

    private static final Logger log = JmqttLogger.eventLog;
    private static final AtomicLong offset = new AtomicLong();
    private static final String currentIp = MixAll.getLocalIp();

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
        Long maxId = (Long) operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return getMapper(sqlSession,eventMapperClass).getMaxOffset();
            }
        });
        if (maxId == null) {
            offset.set(0);
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
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession,eventMapperClass).sendEvent(eventDO));
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
        List<EventDO> eventDOList = (List<EventDO>) operate(sqlSession -> getMapper(sqlSession,eventMapperClass).consumeEvent(currentOffset,maxPollNum));
        if (eventDOList == null || eventDOList.size() == 0) {
            return Collections.emptyList();
        }
        List<Event> events = new ArrayList<>();
        for (EventDO eventDO : eventDOList) {
            Event event = new Event(eventDO.getEventCode(),eventDO.getContent(),eventDO.getGmtCreate(),currentIp);
            events.add(event);
        }

        // reset offset
        EventDO eventDO = eventDOList.get(eventDOList.size()-1);
        if (!offset.compareAndSet(currentOffset,eventDO.getId())) {
            LogUtil.warn(log,"[RDBClusterEventHandler] pollEvent offset is wrong,expectOffset:{},currentOffset:{},maxOffset:{}",
                    offset.get(),currentOffset,eventDO.getId());
            offset.set(eventDO.getId());
        }
        return events;
    }
}
