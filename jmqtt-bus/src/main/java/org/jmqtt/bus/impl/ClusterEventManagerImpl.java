package org.jmqtt.bus.impl;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.ClusterEventManager;
import org.jmqtt.bus.model.ClusterEvent;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.EventDO;

public class ClusterEventManagerImpl implements ClusterEventManager {

    @Override
    public void sendEvent(ClusterEvent clusterEvent) {
        EventDO eventDO = convert(clusterEvent);
        DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.eventMapperClass).sendEvent(eventDO);
            }
        });
    }


    private EventDO convert(ClusterEvent clusterEvent) {
        EventDO eventDO = new EventDO();
        eventDO.setContent(clusterEvent.getContent());
        eventDO.setNodeIp(clusterEvent.getNodeIp());
        eventDO.setEventCode(clusterEvent.getClusterEventCode().getCode());
        eventDO.setGmtCreate(clusterEvent.getGmtCreate());
        return eventDO;
    }
}
