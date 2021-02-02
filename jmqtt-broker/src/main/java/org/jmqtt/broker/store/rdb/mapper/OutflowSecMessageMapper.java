package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.OutflowSecMessageDO;

import java.util.List;

public interface OutflowSecMessageMapper {

    @Insert("INSERT INTO jmqtt_outflow_sec_message(client_id,msg_id,gmt_create) VALUES(#{clientId},#{msgId},#{gmtCreate})")
    Long cacheOuflowMessage(OutflowSecMessageDO outflowSecMessageDO);

    @Select("SELECT id,client_id,msg_id,gmt_create FROM jmqtt_outflow_sec_message WHERE client_id = #{clientId} and msg_id = #{msgId}")
    OutflowSecMessageDO getOutflowSecMessage(String clientId,int msgId);

    @Delete("DELETE FROM jmqtt_outflow_sec_message WHERE id = #{id}")
    Integer delOutflowSecMessage(Long id);

    @Select("SELECT msg_id FROM jmqtt_outflow_sec_message WHERE client_id = #{clientId} order by gmt_create asc")
    List<Integer> getAllOutflowSecMessage(String clientId);
}
