package org.jmqtt.mqtt.retain.impl;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.store.DBCallback;
import org.jmqtt.bus.store.DBUtils;
import org.jmqtt.bus.store.daoobject.RetainMessageDO;
import org.jmqtt.mqtt.retain.RetainMessageHandler;
import org.jmqtt.support.helper.MixAll;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RetainMessageHandlerImpl implements RetainMessageHandler {

    private static final Logger log = JmqttLogger.mqttLog;

    @Override
    public List<DeviceMessage> getAllRetatinMessage() {

        // If has too much retain message ,can optimize with paging query
        List<RetainMessageDO> retainMessageDOList =  DBUtils.operate(new DBCallback() {
            @Override
            public List<RetainMessageDO> operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.retainMessageMapperClass).getAllRetainMessage();
            }
        });
        if (retainMessageDOList == null) {
            return null;
        }
        List<DeviceMessage> deviceMessageList = new ArrayList<>();
        retainMessageDOList.forEach(item -> {
            DeviceMessage deviceMessage = new DeviceMessage();
            deviceMessage.setId(item.getId());
            deviceMessage.setProperties(MixAll.propToMap(item.getProperties()));
            deviceMessage.setContent(item.getContent());
            deviceMessage.setFromClientId(item.getFromClientId());
            deviceMessage.setStoredTime(item.getStoredTime());
            deviceMessage.setTopic(item.getTopic());
            deviceMessageList.add(deviceMessage);
        });
        return deviceMessageList;
    }

    @Override
    public void clearRetainMessage(String topic) {
        DBUtils.operate(new DBCallback() {
            @Override
            public Integer operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.retainMessageMapperClass).delRetainMessage(topic);
            }
        });
    }

    @Override
    public void storeRetainMessage(DeviceMessage deviceMessage) {
        RetainMessageDO retainMessageDO = convert(deviceMessage);
        DBUtils.operate(new DBCallback() {
            @Override
            public Long operate(SqlSession sqlSession) {
                return DBUtils.getMapper(sqlSession,DBUtils.retainMessageMapperClass).storeRetainMessage(retainMessageDO);
            }
        });
        if (retainMessageDO.getId() == null) {
            LogUtil.error(log,"[RETAIN MESSAGE] store retain message index error.");
        }
    }

    private RetainMessageDO convert(DeviceMessage deviceMessage) {
        RetainMessageDO retainMessageDO = new RetainMessageDO();
        retainMessageDO.setTopic(deviceMessage.getTopic());
        retainMessageDO.setFromClientId(deviceMessage.getFromClientId());
        retainMessageDO.setContent(deviceMessage.getContent());
        retainMessageDO.setStoredTime(deviceMessage.getStoredTime());
        retainMessageDO.setProperties(MixAll.propToStr(deviceMessage.getProperties()));
        return retainMessageDO;
    }

}
