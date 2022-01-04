package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.bus.store.daoobject.RetainMessageDO;

import java.util.List;

public interface RetainMessageMapper {

    @Insert("INSERT INTO jmqtt_retain_message(topic,content,from_client_id,stored_time,properties) VALUES"
            + "(#{topic},#{content},#{fromClientId},#{storedTime},#{properties})"
            + " on DUPLICATE key update content = #{content},from_client_id = #{fromClientId},stored_time=#{storedTime},properties = #{properties}")
    @Options(useGeneratedKeys=true,keyProperty="id")
    Long storeRetainMessage(RetainMessageDO retainMessageDO);

    @Select("SELECT topic,content,from_client_id,stored_time,properties FROM jmqtt_retain_message")
    List<RetainMessageDO> getAllRetainMessage();

    @Delete("DELETE FROM jmqtt_retain_message WHERE topic = #{topic}")
    Integer delRetainMessage(String topic);
}
