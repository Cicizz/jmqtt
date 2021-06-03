package org.jmqtt.broker.store.rdb;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.TenantContext;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.rdb.daoobject.RetainMessageTenant;
import org.jmqtt.broker.store.rdb.daoobject.WillMessageTenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RDBMessageStore extends AbstractDBStore implements MessageStore {

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }


    @Override
    public boolean storeWillMessage(String clientId, Message message) {
        WillMessageTenant willMessageDO = new WillMessageTenant();
        willMessageDO.setClientId(clientId);
        willMessageDO.setContent(JSONObject.toJSONString(message));
        willMessageDO.setGmtCreate(message.getStoreTime());
        willMessageDO.setBizCode(TenantContext.getBizCode());
        willMessageDO.setTenantCode(TenantContext.getTenantCode());

        Long id = (Long) operate(sqlSession -> getMapper(sqlSession,willMessageMapperClass).storeWillMessage(willMessageDO));
        return id != 0;
    }

    @Override
    public boolean clearWillMessage(String clientId) {
        operate(sqlSession -> getMapper(sqlSession,willMessageMapperClass).delWillMessage(clientId));
        return true;
    }

    @Override
    public Message getWillMessage(String clientId) {
        WillMessageTenant willMessageDO = (WillMessageTenant) operate(sqlSession -> getMapper(sqlSession,willMessageMapperClass).getWillMessage(clientId));
        if (willMessageDO == null) {
            return null;
        }
        return JSONObject.parseObject(willMessageDO.getContent(),Message.class);
    }

    @Override
    public boolean storeRetainMessage(String topic, Message message) {
        RetainMessageTenant retainMessageDO = new RetainMessageTenant();
        retainMessageDO.setTopic(topic);
        retainMessageDO.setContent(JSONObject.toJSONString(message));
        retainMessageDO.setBizCode(message.getBizCode());
        retainMessageDO.setTenantCode(message.getTenantCode());
        Long id = (Long) operate(new DBCallback() {
            @Override
            public Object operate(SqlSession sqlSession) {
                return getMapper(sqlSession,retainMessageMapperClass).storeRetainMessage(retainMessageDO);
            }
        });
        return id != 0;
    }

    @Override
    public boolean clearRetainMessage(String topic) {
        operate(sqlSession -> getMapper(sqlSession,retainMessageMapperClass).delRetainMessage(topic));
        return true;
    }

    @Override
    public Collection<Message> getAllRetainMsg() {
        List<RetainMessageTenant> messageList = (List<RetainMessageTenant>) operate(sqlSession -> getMapper(sqlSession,retainMessageMapperClass).getAllRetainMessage());
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (RetainMessageTenant retainMessageDO : messageList) {
            Message message = JSONObject.parseObject(retainMessageDO.getContent(),Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }
}
