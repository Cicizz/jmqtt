package org.jmqtt.broker.remoting.session;

import org.apache.ibatis.session.SqlSession;
import org.jmqtt.broker.common.model.AuthorityEnum;
import org.jmqtt.broker.common.model.Constants;
import org.jmqtt.broker.store.rdb.AbstractDBStore;
import org.jmqtt.broker.store.rdb.DBCallback;
import org.jmqtt.broker.store.rdb.daoobject.ProductTopicDO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager extends AbstractDBStore {

    private Map<String /* tenantCode@topic */, TopicPermission> topicPermissionMap = new ConcurrentHashMap<>();

    private static final AuthManager INSTANCE = new AuthManager();

    private AuthManager() {}

    public static AuthManager getInstance() {
        return INSTANCE;
    }

    public TopicPermission getTopicPerm(String tenantCode, String topic) {
        TopicPermission topicPermission = topicPermissionMap.get(tenantCode + Constants.SPLIT + topic);
        if (topicPermission == null) {
            ProductTopicDO productTopicDO = (ProductTopicDO) operate(new DBCallback() {
                @Override
                public Object operate(SqlSession sqlSession) {
                    ProductTopicDO query = new ProductTopicDO();
                    query.setTopic(topic);
                    query.setTenantCode(tenantCode);
                    return getMapper(sqlSession, productTopicMapperClass).getProductTopicByTopic(query);
                }
            });
            if (productTopicDO == null) {
                return null;
            }
            topicPermission = new TopicPermission();
            topicPermission.setTenantCode(productTopicDO.getTenantCode());
            topicPermission.setAuthority(productTopicDO.getAuthority());
            topicPermission.setProductId(productTopicDO.getProductId());
            topicPermission.setTopic(productTopicDO.getTopic());
            putTopicPerm(topicPermission);
            return topicPermission;
        }
        return topicPermission;
    }

    public boolean hasPub(String tenantCode,String topic){
        TopicPermission topicPermission = getTopicPerm(tenantCode,topic);
        if (topicPermission == null) {
            return false;
        }
        return AuthorityEnum.hasPub(topicPermission.getAuthority());
    }

    public boolean hasSub(String tenantCode,String topic){
        TopicPermission topicPermission = getTopicPerm(tenantCode,topic);
        if (topicPermission == null) {
            return false;
        }
        return AuthorityEnum.hasSub(topicPermission.getAuthority());
    }

    public TopicPermission putTopicPerm(TopicPermission topicPermission) {
        return this.topicPermissionMap.put(topicPermission.getTenantCode() + Constants.SPLIT + topicPermission.getTopic(), topicPermission);
    }

    public TopicPermission removeTopicPerm(Long tenantCode, String topic) {
        return topicPermissionMap.remove(tenantCode + Constants.SPLIT + topic);
    }
}
