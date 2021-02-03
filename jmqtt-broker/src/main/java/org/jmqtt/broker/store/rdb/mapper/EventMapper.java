package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.EventDO;

import java.util.List;

public interface EventMapper {

    @Insert("insert into jmqtt_event content,gmt_create,jmqtt_ip,event_code values "
            + "(#{content},#{gmtCreate},#{jmqttIp},#{eventCode})")
    Long sendEvent(EventDO eventDO);


    @Select("select id,content,gmt_create,jmqtt_ip,event_code from jmqtt_event "
            + "where id > #{offset} order by id asc limit #{maxNum}")
    List<EventDO> consumeEvent(long offset,int maxNum);

    @Select("SELECT max(id) FROM jmqtt_event")
    Long getMaxOffset();
}
