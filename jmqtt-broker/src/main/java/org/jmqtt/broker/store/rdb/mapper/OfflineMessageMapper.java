package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.OfflineMessageDO;

import java.util.List;

public interface OfflineMessageMapper {

    @Insert("INSERT INTO jmqtt_offline_message(client_id,content,gmt_create) VALUES (#{clientId},#{content},#{gmtCreate})")
    Long storeOfflineMessage(OfflineMessageDO offlineMessageDO);

    @Delete("DELETE FROM jmqtt_offline_message WHERE client_id = #{clientId}")
    Integer clearOfflineMessage(String clientId);

    @Select("SELECT id,client_id,content,gmt_create FROM jmqtt_offline_message WHERE client_id = #{clientId} order by gmt_create asc")
    List<OfflineMessageDO> getAllOfflineMessage(String clientId);
}
