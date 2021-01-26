
package org.jmqtt.broker.store.rdb;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.store.rdb.mapper.SessionMapper;

import javax.sql.DataSource;

public class MybatisHandler {

    public void start(BrokerConfig brokerConfig){

        DataSource dataSource = new DruidDataSource();;
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(
                SessionMapper.class
        );
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }
}
