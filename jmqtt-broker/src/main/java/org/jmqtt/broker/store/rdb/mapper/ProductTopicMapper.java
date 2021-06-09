package org.jmqtt.broker.store.rdb.mapper;

import org.apache.ibatis.annotations.Select;
import org.jmqtt.broker.store.rdb.daoobject.ProductTopicDO;

import java.util.List;

public interface ProductTopicMapper {

    @Select("SELECT product_id,topic,authority FROM jmq_product_topic WHERE product_id = #{productId} and tenant_code = #{tenantCode}")
    List<ProductTopicDO> getProductTopicByProductId(ProductTopicDO productTopicDO);


    @Select("SELECT product_id,topic,authority,tenant_code,biz_code FROM jmq_product_topic WHERE topic = #{topic} and tenant_code = #{tenantCode}")
    ProductTopicDO getProductTopicByTopic(ProductTopicDO productTopicDO);
}
