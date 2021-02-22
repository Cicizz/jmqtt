package org.jmqtt.starter.configuration;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.starter.properties.BrokerProperties;
import org.jmqtt.starter.properties.NettyProperties;
import org.jmqtt.starter.service.BrokerStartupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({BrokerProperties.class, NettyProperties.class})
public class BrokerStartupConfiguration {

	@Autowired
	private BrokerProperties brokerProperties;

	@Autowired
	private NettyProperties nettyProperties;

	@Bean
	@ConditionalOnMissingBean
	public BrokerController getBrokerController(){
		return new BrokerStartupService(brokerProperties, nettyProperties).getBrokerController();
	}
}
