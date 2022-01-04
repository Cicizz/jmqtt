package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.*;
import org.jmqtt.bus.store.daoobject.DeviceInboxMessageDO;

import java.util.List;

public interface ClientInboxMessageMapper {

    @Insert("INSERT INTO jmqtt_client_inbox(client_id,message_id,ack,stored_time) VALUES (#{clientId},#{messageId},#{ack},#{storedTime})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    Long addInboxMessage(DeviceInboxMessageDO deviceInboxMessageDO);

    @Update("UPDATE jmqtt_client_inbox set ack = 1,ack_time = now()"
            + "WHERE client_id = #{clientId} and message_id = #{messageId}")
    Integer ack(@Param("clientId") String clientId,@Param("messageId") Long messageId);


    @Update("DELETE FROM jmqtt_client_inbox WHERE client_id = #{clientId} AND ack = 0")
    Integer truncateUnAck(@Param("clientId") String clientId);

    @Select("SELECT * FROM jmqtt_client_inbox WHERE client_id = #{clientId} and ack = 0 ORDER BY stored_time ASC LIMIT #{limit} ")
    List<DeviceInboxMessageDO> getUnAckMessages(@Param("clientId") String clientId, @Param("limit") int limit);
}
