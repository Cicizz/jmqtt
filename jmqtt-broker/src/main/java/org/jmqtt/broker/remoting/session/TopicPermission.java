package org.jmqtt.broker.remoting.session;

import lombok.Data;
import lombok.ToString;
import org.jmqtt.broker.store.rdb.daoobject.TenantBase;

import java.io.Serializable;

@Data
@ToString
public class TopicPermission extends TenantBase implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;


    private String topic;

    private Long productId;

    private String authority;
}
