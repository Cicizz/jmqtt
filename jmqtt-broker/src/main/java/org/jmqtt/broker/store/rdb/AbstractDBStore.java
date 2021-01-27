package org.jmqtt.broker.store.rdb;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDBStore {

    protected final static Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    protected void start(BrokerConfig brokerConfig){
        DBUtils.getInstance().start(brokerConfig);
    }

    protected void shutdown() {
        DBUtils.getInstance().shutdown();
    }

    protected <T> T getMapper(Class<T> clazz) {
        return DBUtils.getInstance().getMapper(clazz);
    }

    protected SqlSession getSessionWithTrans(){
        return DBUtils.getInstance().getSessionWithTrans();
    }
}
