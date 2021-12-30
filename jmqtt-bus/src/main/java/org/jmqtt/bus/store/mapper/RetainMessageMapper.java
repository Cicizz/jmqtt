package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.bus.store.daoobject.RetainMessageDO;

import java.util.List;

public interface RetainMessageMapper {

    @Insert("INSERT INTO jmqtt_retain_message(topic,message_id) VALUES(#{topic},#{messageId})"
            + " on DUPLICATE key update message_id = #{messageId}")
    Long storeRetainMessage(RetainMessageDO retainMessageDO);

    @Select("SELECT id,topic,message_id FROM jmqtt_retain_message")
    List<RetainMessageDO> getAllRetainMessage();

    @Delete("DELETE FROM jmqtt_retain_message WHERE topic = #{topic}")
    Integer delRetainMessage(String topic);
}
