package org.jmqtt.broker.store.rdb;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.store.rdb.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDBStore {

    static final Class<SessionMapper>           sessionMapperClass           = SessionMapper.class;
    static final Class<SubscriptionMapper>      subscriptionMapperClass      = SubscriptionMapper.class;
    static final Class<EventMapper>             eventMapperClass             = EventMapper.class;
    static final Class<InflowMessageMapper>     inflowMessageMapperClass     = InflowMessageMapper.class;
    static final Class<OutflowMessageMapper>    outflowMessageMapperClass    = OutflowMessageMapper.class;
    static final Class<OutflowSecMessageMapper> outflowSecMessageMapperClass = OutflowSecMessageMapper.class;
    static final Class<RetainMessageMapper>     retainMessageMapperClass     = RetainMessageMapper.class;

    static final Class<OfflineMessageMapper> offlineMessageMapperClass = OfflineMessageMapper.class;
    static final Class<WillMessageMapper>    willMessageMapperClass    = WillMessageMapper.class;

    final static Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    void start(BrokerConfig brokerConfig) {
        DBUtils.getInstance().start(brokerConfig);
    }

    void shutdown() {
        DBUtils.getInstance().shutdown();
    }

    <T> T getMapper(Class<T> clazz) {
        return DBUtils.getInstance().getMapper(clazz);
    }

    SqlSession getSessionWithTrans() {
        return DBUtils.getInstance().getSessionWithTrans();
    }
}
