package org.jmqtt.bus.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.DeviceMessageManager;
import org.jmqtt.bus.enums.MessageSourceEnum;
import org.jmqtt.bus.model.DeviceInboxMessage;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.DeviceInboxMessageDO;
import org.jmqtt.bus.store.daoobject.MessageDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceMessageManagerImpl implements DeviceMessageManager {


    @Override
    public void clearOfflineMessage(String clientId) {

    }

    @Override
    public void dispatcher(DeviceMessage deviceMessage) {

    }

    @Override
    public List<DeviceMessage> queryUnAckMessages(String clientId, int pageSize) {
        List<DeviceInboxMessageDO> inboxMessageDOS = (List<DeviceInboxMessageDO>) DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.clientInboxMessageMapperClass).getUnAckMessages(clientId,pageSize);
            }
        });

        if (inboxMessageDOS == null) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        inboxMessageDOS.forEach(itemDO -> {
            ids.add(itemDO.getId());
        });

        List<MessageDO> messages = (List<MessageDO>) DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.messageMapperClass).queryMessageByIds(ids);
            }
        });

        List<DeviceMessage> deviceMessageList = new ArrayList<>(messages.size());
        messages.forEach(item -> {
            deviceMessageList.add(convert(item));
        });

        return deviceMessageList;
    }

    @Override
    public boolean ackMessage(String clientId, Long messageId) {
        Integer effect = (Integer) DBUtils.operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.clientInboxMessageMapperClass).ack(clientId,messageId);
            }
        });
        return effect > 0;
    }

    private DeviceMessage convert(MessageDO messageDO) {
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setTopic(messageDO.getTopic());
        deviceMessage.setStoredTime(messageDO.getStoredTime());
        deviceMessage.setSource(MessageSourceEnum.valueOf(messageDO.getSource()));
        deviceMessage.setContent(messageDO.getContent());
        deviceMessage.setId(messageDO.getId());
        deviceMessage.setFromClientId(messageDO.getFromClientId());

        String properties = messageDO.getProperties();
        if (properties != null) {
            deviceMessage.setProperties(JSONObject.parseObject(properties, Map.class));
        }
        return deviceMessage;
    }


    private DeviceInboxMessage convert(DeviceInboxMessageDO deviceInboxMessageDO) {
        DeviceInboxMessage deviceInboxMessage = new DeviceInboxMessage();
        deviceInboxMessage.setAck(deviceInboxMessageDO.getAck());
        deviceInboxMessage.setAckTime(deviceInboxMessageDO.getAckTime());
        deviceInboxMessage.setClientId(deviceInboxMessageDO.getClientId());
        deviceInboxMessage.setId(deviceInboxMessageDO.getId());
        deviceInboxMessage.setMessageId(deviceInboxMessageDO.getMessageId());
        deviceInboxMessage.setStoredTime(deviceInboxMessageDO.getStoredTime());
        return deviceInboxMessage;
    }
}
