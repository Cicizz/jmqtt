package org.jmqtt.broker.store.rdb;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.rdb.daoobject.RetainMessageDO;
import org.jmqtt.broker.store.rdb.daoobject.WillMessageDO;

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
        WillMessageDO willMessageDO = new WillMessageDO();
        willMessageDO.setClientId(clientId);
        willMessageDO.setContent(JSONObject.toJSONString(message));
        willMessageDO.setGmtCreate(message.getStoreTime());

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
        WillMessageDO willMessageDO = (WillMessageDO) operate(sqlSession -> getMapper(sqlSession,willMessageMapperClass).getWillMessage(clientId));
        if (willMessageDO == null) {
            return null;
        }
        return JSONObject.parseObject(willMessageDO.getContent(),Message.class);
    }

    @Override
    public boolean storeRetainMessage(String topic, Message message) {
        RetainMessageDO retainMessageDO = new RetainMessageDO();
        retainMessageDO.setTopic(topic);
        retainMessageDO.setContent(JSONObject.toJSONString(message));
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
        List<RetainMessageDO> messageList = (List<RetainMessageDO>) operate(sqlSession -> getMapper(sqlSession,retainMessageMapperClass).getAllRetainMessage());
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (RetainMessageDO retainMessageDO : messageList) {
            Message message = JSONObject.parseObject(retainMessageDO.getContent(),Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }
}
