package org.jmqtt.broker.store.rdb;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.store.rdb.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDBStore {

    protected static final Class<SessionMapper>           sessionMapperClass           = SessionMapper.class;
    protected static final Class<SubscriptionMapper>      subscriptionMapperClass      = SubscriptionMapper.class;
    protected static final Class<EventMapper>             eventMapperClass             = EventMapper.class;
    protected static final Class<InflowMessageMapper>     inflowMessageMapperClass     = InflowMessageMapper.class;
    protected static final Class<OutflowMessageMapper>    outflowMessageMapperClass    = OutflowMessageMapper.class;
    protected static final Class<OutflowSecMessageMapper> outflowSecMessageMapperClass = OutflowSecMessageMapper.class;
    protected static final Class<RetainMessageMapper>     retainMessageMapperClass     = RetainMessageMapper.class;

    protected static final Class<OfflineMessageMapper> offlineMessageMapperClass = OfflineMessageMapper.class;
    protected static final Class<WillMessageMapper>    willMessageMapperClass    = WillMessageMapper.class;

    protected final static Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    protected void start(BrokerConfig brokerConfig) {
        DBUtils.getInstance().start(brokerConfig);
    }

    protected void shutdown() {
        DBUtils.getInstance().shutdown();
    }

    protected <T> T getMapper(Class<T> clazz) {
        return DBUtils.getInstance().getMapper(clazz);
    }

    protected SqlSession getSessionWithTrans() {
        return DBUtils.getInstance().getSessionWithTrans();
    }
}
