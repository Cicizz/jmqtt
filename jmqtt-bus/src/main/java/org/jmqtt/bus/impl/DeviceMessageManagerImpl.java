package org.jmqtt.bus.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.DeviceMessageManager;
import org.jmqtt.bus.enums.ClusterEventCodeEnum;
import org.jmqtt.bus.enums.MessageAckEnum;
import org.jmqtt.bus.enums.MessageSourceEnum;
import org.jmqtt.bus.model.DeviceInboxMessage;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.DeviceInboxMessageDO;
import org.jmqtt.bus.store.daoobject.EventDO;
import org.jmqtt.bus.store.daoobject.MessageDO;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeviceMessageManagerImpl implements DeviceMessageManager {

    private static final Logger log = JmqttLogger.busLog;

    @Override
    public void clearUnAckMessage(String clientId) {
        DBUtils.operate(new DBCallback() {
            @Override
            public Integer operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.clientInboxMessageMapperClass).truncateUnAck(clientId);
            }
        });
    }

    @Override
    public void dispatcher(DeviceMessage deviceMessage) {

        // store message and send event
        SqlSession sqlSession = null;
        try {
            sqlSession = DBUtils.getSqlSessionWithTrans();
            MessageDO messageDO = convert(deviceMessage);
            DBUtils.getMapper(sqlSession,DBUtils.messageMapperClass).storeMessage(messageDO);
            Long id = messageDO.getId();
            if (id == null || id <= 0) {
                LogUtil.error(log,"[BUS] store message failure.");
                return;
            }
            deviceMessage.setId(id);
            EventDO eventDO = convert2Event(deviceMessage);
            DBUtils.getMapper(sqlSession,DBUtils.eventMapperClass).sendEvent(eventDO);
            Long eventId = eventDO.getId();
            if (eventId == null || eventId <= 0) {
                LogUtil.error(log,"[BUS] send event message failure.");
                sqlSession.rollback();
                return;
            }
            sqlSession.commit();
        } catch (Exception ex) {
            LogUtil.error(log,"[BUS] dispatcher message store caught exception,{}",ex);
            if (sqlSession != null) {
                sqlSession.rollback();
            }
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    @Override
    public Long storeMessage(DeviceMessage deviceMessage) {
        MessageDO messageDO = convert(deviceMessage);
        DBUtils.operate(new DBCallback() {
            @Override
            public Long operate(SqlSession sqlSession) {
                 return DBUtils.getMapper(sqlSession,DBUtils.messageMapperClass).storeMessage(messageDO);
            }
        });
        return messageDO.getId();
    }

    @Override
    public List<DeviceMessage> queryUnAckMessages(String clientId, int pageSize) {
        List<DeviceInboxMessageDO> inboxMessageDOS =  DBUtils.operate(new DBCallback() {
            @Override
            public List<DeviceInboxMessageDO> operate(SqlSession sqlSession) {
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
        return queryByIds(ids);
    }

    @Override
    public List<DeviceMessage> queryByIds(List<Long> ids) {
        List<MessageDO> messages =  DBUtils.operate(new DBCallback() {
            @Override
            public List<MessageDO> operate(SqlSession sqlSession) {
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
    public Long addClientInBoxMsg(String clientId, Long messageId, MessageAckEnum ackEnum) {

        DeviceInboxMessageDO deviceInboxMessageDO = new DeviceInboxMessageDO();
        deviceInboxMessageDO.setAck(ackEnum.getCode());
        deviceInboxMessageDO.setClientId(clientId);
        deviceInboxMessageDO.setMessageId(messageId);
        deviceInboxMessageDO.setStoredTime(new Date());

        DBUtils.operate(new DBCallback() {
            @Override
            public Long operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.clientInboxMessageMapperClass).addInboxMessage(deviceInboxMessageDO);
            }
        });
        return deviceInboxMessageDO.getId();
    }

    @Override
    public boolean ackMessage(String clientId, Long messageId) {
        Integer effect = DBUtils.operate(new DBCallback() {
            @Override
            public Integer operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession, DBUtils.clientInboxMessageMapperClass).ack(clientId,messageId);
            }
        });
        return effect > 0;
    }

    private EventDO convert2Event(DeviceMessage deviceMessage) {
        EventDO eventDO = new EventDO();
        eventDO.setNodeIp(RemotingHelper.getLocalAddr());
        eventDO.setContent(JSONObject.toJSONString(deviceMessage));
        eventDO.setEventCode(ClusterEventCodeEnum.DISPATCHER_CLIENT_MESSAGE.getCode());
        eventDO.setGmtCreate(new Date());
        return eventDO;
    }

    private MessageDO convert(DeviceMessage deviceMessage) {
        MessageDO messageDO = new MessageDO();
        messageDO.setContent(deviceMessage.getContent());
        messageDO.setFromClientId(deviceMessage.getFromClientId());
        messageDO.setSource(deviceMessage.getSource().getCode());
        messageDO.setStoredTime(deviceMessage.getStoredTime());
        messageDO.setTopic(deviceMessage.getTopic());
        if (deviceMessage.getProperties() != null) {
            messageDO.setProperties(JSONObject.toJSONString(deviceMessage.getProperties()));
        }
        return messageDO;
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
