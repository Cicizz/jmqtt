package org.jmqtt.bus.store;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.bus.store.mapper.*;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.log.JmqttLogger;
import org.slf4j.Logger;

public abstract class AbstractDBStore {



    protected final static Logger log = JmqttLogger.storeLog;

    protected void start(BrokerConfig brokerConfig) {
        DBUtils.getInstance().start(brokerConfig);
    }

    protected void shutdown() {
        DBUtils.getInstance().shutdown();
    }

    protected <T> T getMapper(SqlSession sqlSession,Class<T> clazz) {
        return sqlSession.getMapper(clazz);
    }

    protected Object operate(DBCallback dbCallback){
       return DBUtils.getInstance().operate(dbCallback);
    }

    public SqlSession getSqlSessionWithTrans() {
        return DBUtils.getInstance().getSqlSessionWithTrans();
    }
}
