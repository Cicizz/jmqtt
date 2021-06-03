package org.jmqtt.broker.common.model;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TenantInfo {

    private String bizCode;

    private String tenantCode;

    private Channel channel;

    private String clientId;
}
