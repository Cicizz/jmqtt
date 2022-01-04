package org.jmqtt.bus.store.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jmqtt.bus.store.daoobject.MessageDO;

import java.util.List;

public interface MessageMapper {

    @Insert("INSERT INTO jmqtt_message(id,source,content,topic,from_client_id,stored_time,properties) VALUES(#{id},#{source},#{content}"
            + ",#{topic},#{fromClientId},#{storedTime},#{properties})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    Long storeMessage(MessageDO messageDO);

    @Select("SELECT id,source,content,topic,from_client_id,stored_time,properties FROM jmqtt_message WHERE id = #{id}")
    MessageDO getMessage(@Param("id") long id);

    @Select("<script>SELECT id,source,content,topic,from_client_id,stored_time,properties FROM jmqtt_message "
            + "where id in "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>"
            + "#{id}"
            + "</foreach>  "
            + "</script>")
    List<MessageDO> queryMessageByIds(@Param("ids")List<Long> ids);
}
