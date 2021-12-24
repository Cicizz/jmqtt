
package org.jmqtt.broker.store.rdb;

import com.alibaba.fastjson.JSONObject;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;
import org.jmqtt.broker.store.SessionState;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.rdb.daoobject.*;

import java.util.*;

public class RDBSessionStore extends AbstractDBStore implements SessionStore {

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public SessionState getSession(String clientId) {
        SessionDO sessionDO = (SessionDO) operate(sqlSession -> getMapper(sqlSession, sessionMapperClass).getSession(clientId));
        if (sessionDO == null) {
            return new SessionState(SessionState.StateEnum.NULL);
        }
        return new SessionState(SessionState.StateEnum.valueOf(sessionDO.getState()), sessionDO.getOfflineTime());
    }

    @Override
    public boolean storeSession(String clientId, SessionState sessionState) {
        SessionDO sessionDO = new SessionDO();
        sessionDO.setClientId(clientId);
        sessionDO.setState(sessionState.getState().getCode());
        sessionDO.setOfflineTime(sessionState.getOfflineTime());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, sessionMapperClass).storeSession(sessionDO));
        return id != null;
    }

    @Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        SubscriptionDO subscriptionDO = new SubscriptionDO();
        subscriptionDO.setClientId(clientId);
        subscriptionDO.setTopic(subscription.getTopic());
        subscriptionDO.setQos(subscription.getQos());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, subscriptionMapperClass).storeSubscription(subscriptionDO));

        return id != null;
    }

    @Override
    public boolean clearSubscription(String clientId) {
        Integer effectNum = (Integer) operate(sqlSession -> getMapper(sqlSession, subscriptionMapperClass).clearSubscription(clientId));
        LogUtil.debug(log, "[ClearSubscription] effect num:{}", effectNum);
        return true;
    }

    @Override
    public boolean delSubscription(String clientId, String topic) {
        Integer effectNum = (Integer) operate(
                sqlSession -> getMapper(sqlSession, subscriptionMapperClass).delSubscription(clientId, topic));
        if (effectNum != null && effectNum > 0) {
            return true;
        }
        LogUtil.debug(log, "[DelSubscription]  subscription is not exist:{},{}", clientId, topic);
        return false;
    }

    @Override
    public Set<Subscription> getSubscriptions(String clientId) {
        List<SubscriptionDO> subscriptionDOList = (List<SubscriptionDO>) operate(
                sqlSession -> getMapper(sqlSession, subscriptionMapperClass).querySubscription(clientId));
        Set<Subscription> set = new HashSet<>();
        for (SubscriptionDO item : subscriptionDOList) {
            Subscription subscription = new Subscription(item.getClientId(), item.getTopic(), item.getQos());
            set.add(subscription);
        }
        return set;
    }

    @Override
    public boolean cacheInflowMsg(String clientId, Message message) {
        InflowMessageDO inflowMessageDO = new InflowMessageDO();
        inflowMessageDO.setClientId(clientId);
        inflowMessageDO.setMsgId(message.getMsgId());
        inflowMessageDO.setContent(JSONObject.toJSONString(message));
        inflowMessageDO.setGmtCreate(message.getStoreTime());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, inflowMessageMapperClass).cacheInflowMessage(inflowMessageDO));
        return id != null;
    }

    @Override
    public Message releaseInflowMsg(String clientId, int msgId) {
        InflowMessageDO inflowMessageDO = (InflowMessageDO) operate(
                sqlSession -> getMapper(sqlSession, inflowMessageMapperClass).getInflowMessage(clientId, msgId));
        if (inflowMessageDO == null) {
            return null;
        }
        Integer effecuNum = (Integer) operate(
                sqlSession -> getMapper(sqlSession, inflowMessageMapperClass).delInflowMessage(inflowMessageDO.getId()));
        if (effecuNum == null || effecuNum == 0) {
            LogUtil.warn(log, "releaseInflowMsg del inflow msg error,{},{}", clientId, msgId);
        }
        return JSONObject.parseObject(inflowMessageDO.getContent(), Message.class);
    }

    @Override
    public Collection<Message> getAllInflowMsg(String clientId) {
        List<InflowMessageDO> messageList = (List<InflowMessageDO>) operate(
                sqlSession -> getMapper(sqlSession, inflowMessageMapperClass).getAllInflowMessage(clientId));
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (InflowMessageDO inflowMessageDO : messageList) {
            Message message = JSONObject.parseObject(inflowMessageDO.getContent(), Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }

    @Override
    public boolean cacheOutflowMsg(String clientId, Message message) {
        OutflowMessageDO outflowMessageDO = new OutflowMessageDO();
        outflowMessageDO.setClientId(clientId);
        outflowMessageDO.setMsgId(message.getMsgId());
        outflowMessageDO.setContent(JSONObject.toJSONString(message));
        outflowMessageDO.setGmtCreate(message.getStoreTime());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, outflowMessageMapperClass).cacheOuflowMessage(outflowMessageDO));
        return id != null;
    }

    @Override
    public Collection<Message> getAllOutflowMsg(String clientId) {
        List<OutflowMessageDO> messageList = (List<OutflowMessageDO>) operate(
                sqlSession -> getMapper(sqlSession, outflowMessageMapperClass).getAllOutflowMessage(clientId));
        if (MixAll.isEmpty(messageList)) {
            return null;
        }
        List<Message> mqttMessages = new ArrayList<>(messageList.size());
        for (OutflowMessageDO outflowMessageDO : messageList) {
            Message message = JSONObject.parseObject(outflowMessageDO.getContent(), Message.class);
            mqttMessages.add(message);
        }
        return mqttMessages;
    }

    @Override
    public Message releaseOutflowMsg(String clientId, int msgId) {
        OutflowMessageDO outflowMessageDO = (OutflowMessageDO) operate(
                sqlSession -> getMapper(sqlSession, outflowMessageMapperClass).getOutflowMessage(clientId, msgId));
        if (outflowMessageDO == null) {
            return null;
        }
        Integer effecuNum = (Integer) operate(
                sqlSession -> getMapper(sqlSession, outflowMessageMapperClass).delOutflowMessage(outflowMessageDO.getId()));
        if (effecuNum == null || effecuNum == 0) {
            LogUtil.warn(log, "releaseOutflowMsg del outflow msg error,{},{}", clientId, msgId);
        }
        return JSONObject.parseObject(outflowMessageDO.getContent(), Message.class);
    }

    @Override
    public boolean cacheOutflowSecMsgId(String clientId, int msgId) {
        OutflowSecMessageDO outflowSecMessageDO = new OutflowSecMessageDO();
        outflowSecMessageDO.setClientId(clientId);
        outflowSecMessageDO.setMsgId(msgId);
        outflowSecMessageDO.setGmtCreate(System.currentTimeMillis());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, outflowSecMessageMapperClass).cacheOuflowMessage(outflowSecMessageDO));
        return id != null;
    }

    @Override
    public boolean releaseOutflowSecMsgId(String clientId, int msgId) {
        OutflowSecMessageDO outflowSecMessageDO = (OutflowSecMessageDO) operate(
                sqlSession -> getMapper(sqlSession, outflowSecMessageMapperClass).getOutflowSecMessage(clientId, msgId));
        if (outflowSecMessageDO == null) {
            return false;
        }
        Integer effecuNum = (Integer) operate(
                sqlSession -> getMapper(sqlSession, outflowSecMessageMapperClass).delOutflowSecMessage(outflowSecMessageDO.getId()));
        if (effecuNum == null || effecuNum == 0) {
            LogUtil.warn(log, "releaseOutflowSecMsgId del outflow sec msg error,{},{}", clientId, msgId);
        }
        return true;
    }

    @Override
    public List<Integer> getAllOutflowSecMsgId(String clientId) {
        return (List<Integer>) operate(sqlSession -> getMapper(sqlSession, outflowSecMessageMapperClass).getAllOutflowSecMessage(clientId));
    }

    @Override
    public boolean storeOfflineMsg(String clientId, Message message) {
        OfflineMessageDO offlineMessageDO = new OfflineMessageDO();
        offlineMessageDO.setClientId(clientId);
        offlineMessageDO.setContent(JSONObject.toJSONString(message));
        offlineMessageDO.setGmtCreate(message.getStoreTime());
        Long id = (Long) operate(sqlSession -> getMapper(sqlSession, offlineMessageMapperClass).storeOfflineMessage(offlineMessageDO));
        return id != 0;
    }

    @Override
    public Collection<Message> getAllOfflineMsg(String clientId) {
        List<OfflineMessageDO> offlineMessageDOList = (List<OfflineMessageDO>) operate(
                sqlSession -> getMapper(sqlSession, offlineMessageMapperClass).getAllOfflineMessage(clientId));
        if (MixAll.isEmpty(offlineMessageDOList)) {
            return null;
        }
        List<Message> messageList = new ArrayList<>();
        for (OfflineMessageDO offlineMessageDO : offlineMessageDOList) {
            Message message = JSONObject.parseObject(offlineMessageDO.getContent(), Message.class);
            messageList.add(message);
        }
        return messageList;
    }

    @Override
    public boolean clearOfflineMsg(String clientId) {
        Integer effectNum = (Integer) operate(sqlSession -> getMapper(sqlSession, offlineMessageMapperClass).clearOfflineMessage(clientId));
        LogUtil.debug(log, "RDB clearOfflineMsg del nums:{}", effectNum);
        return true;
    }
}
