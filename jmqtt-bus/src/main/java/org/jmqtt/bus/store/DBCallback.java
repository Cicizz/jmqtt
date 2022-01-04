package org.jmqtt.bus.store;

import org.apache.ibatis.session.SqlSession;

public interface DBCallback {
    <T> T operate(SqlSession sqlSession);
}
