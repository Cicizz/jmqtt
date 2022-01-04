package org.jmqtt.bus.store;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.jmqtt.bus.store.mapper.*;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * db 工具类
 */
public class DBUtils {

    private static final Logger log = JmqttLogger.storeLog;

    public static final Class<SessionMapper>            sessionMapperClass           = SessionMapper.class;
    public static final Class<SubscriptionMapper>       subscriptionMapperClass      = SubscriptionMapper.class;
    public static final Class<EventMapper>              eventMapperClass             = EventMapper.class;
    public static final Class<ClientInboxMessageMapper> clientInboxMessageMapperClass     = ClientInboxMessageMapper.class;
    public static final Class<MessageMapper>            messageMapperClass    = MessageMapper.class;
    public static final Class<RetainMessageMapper>      retainMessageMapperClass     = RetainMessageMapper.class;

    private static final DBUtils dbUtils = new DBUtils();

    private DBUtils(){}

    private SqlSessionFactory sqlSessionFactory;

    private  AtomicBoolean start = new AtomicBoolean(false);

    public static DBUtils getInstance(){
        return dbUtils;
    }

    public void start(BrokerConfig brokerConfig){
        if (this.start.compareAndSet(false,true)) {
            LogUtil.info(log,"DB store start...");
            DataSource dataSource = new DataSourceFactory() {
                @Override
                public void setProperties(Properties properties) {
                }

                @Override
                public DataSource getDataSource() {
                    DruidDataSource dds = new DruidDataSource();
                    dds.setDriverClassName(brokerConfig.getDriver());
                    dds.setUrl(brokerConfig.getUrl());
                    dds.setUsername(brokerConfig.getUsername());
                    dds.setPassword(brokerConfig.getPassword());
                    // 其他配置可自行补充
                    dds.setKeepAlive(true);
                    dds.setMinEvictableIdleTimeMillis(180000);
                    dds.setMaxWait(60000);
                    dds.setInitialSize(5);
                    dds.setMinIdle(5);
                    try {
                        dds.init();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                    return dds;
                }
            }.getDataSource();

            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("development", transactionFactory, dataSource);
            Configuration configuration = new Configuration(environment);

            // 初始化所有mapper
            configuration.addMapper(SessionMapper.class);
            configuration.addMapper(SubscriptionMapper.class);
            configuration.addMapper(MessageMapper.class);
            configuration.addMapper(EventMapper.class);
            configuration.addMapper(ClientInboxMessageMapper.class);
            configuration.addMapper(RetainMessageMapper.class);

            configuration.setMapUnderscoreToCamelCase(true);
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
            LogUtil.info(log,"DB store start success...");
        }
    }

    public void shutdown(){}


    public static final <T> T operate(DBCallback dbCallback) {
        try (SqlSession sqlSession = DBUtils.getInstance().sqlSessionFactory.openSession(true)){
            return dbCallback.operate(sqlSession);
        }
    }

    public static final <T> T getMapper(SqlSession sqlSession,Class<T> clazz) {
        return sqlSession.getMapper(clazz);
    }

    /**
     * 获取关闭事物的session，需要手动提交事物
     */
    public static final SqlSession getSqlSessionWithTrans() {
        SqlSession sqlSession = DBUtils.getInstance().sqlSessionFactory.openSession(false);
        return sqlSession;
    }


}
