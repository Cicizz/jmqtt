package org.jmqtt.broker.store.rdb.daoobject;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class DeviceTenant extends TenantBase implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;

    private Long id;

    private String deviceCoding;

    private String deviceKey;

    /**
     * 0在线，1离线
     */
    private Integer isOnline;

    private Date gmtCreate;

    private String latestNetworkAddress;

    private Date latestOnlineTime;

    private Long productId;

}
