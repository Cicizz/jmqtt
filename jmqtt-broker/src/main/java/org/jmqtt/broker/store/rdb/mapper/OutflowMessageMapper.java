package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.OutflowMessageDO;

import java.util.List;

public interface OutflowMessageMapper {

    @Insert("INSERT INTO jmqtt_outflow_message(client_id,msg_id,content,gmt_create) VALUES(#{clientId},#{msgId},#{content},#{gmtCreate})")
    Long cacheOuflowMessage(OutflowMessageDO outflowMessageDO);

    @Select("SELECT id,client_id,msg_id,content,gmt_create FROM jmqtt_outflow_message WHERE client_id = #{clientId} and msg_id = #{msgId}")
    OutflowMessageDO getOutflowMessage(@Param("clientId") String clientId,@Param("msgId") int msgId);

    @Delete("DELETE FROM jmqtt_outflow_message WHERE id = #{id}")
    Integer delOutflowMessage(Long id);

    @Select("SELECT id,client_id,msg_id,content,gmt_create FROM jmqtt_outflow_message WHERE client_id = #{clientId} order by gmt_create asc")
    List<OutflowMessageDO> getAllOutflowMessage(String clientId);
}
