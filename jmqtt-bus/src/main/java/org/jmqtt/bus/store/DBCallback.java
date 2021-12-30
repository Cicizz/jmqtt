package org.jmqtt.bus.store;

import org.apache.ibatis.session.SqlSession;

public interface DBCallback {
    Object operate(SqlSession sqlSession);
}
