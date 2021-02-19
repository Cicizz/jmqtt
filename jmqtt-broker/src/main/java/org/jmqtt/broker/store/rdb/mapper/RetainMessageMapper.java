package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.RetainMessageDO;

import java.util.List;

public interface RetainMessageMapper {

    @Insert("INSERT INTO jmqtt_retain_message(topic,content) VALUES(#{topic},#{content})"
            + " on DUPLICATE key update content = #{content}")
    Long storeRetainMessage(RetainMessageDO retainMessageDO);

    @Select("SELECT id,topic,content FROM jmqtt_retain_message")
    List<RetainMessageDO> getAllRetainMessage();

    @Delete("DELETE FROM jmqtt_retain_message WHERE topic = #{topic}")
    Integer delRetainMessage(String topic);
}
