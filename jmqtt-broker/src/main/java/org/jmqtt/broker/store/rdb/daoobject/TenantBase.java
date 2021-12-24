package org.jmqtt.broker.store.rdb.daoobject;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class TenantBase implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;

    /**
     * 所属业务id
     */
    private String bizCode;

    /**
     * 所属租户id
     */
    private String tenantCode;
}
