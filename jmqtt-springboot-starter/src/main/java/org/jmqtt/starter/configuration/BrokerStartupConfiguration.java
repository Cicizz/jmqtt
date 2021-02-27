package org.jmqtt.starter.configuration;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.config.NettyConfig;
import org.jmqtt.starter.service.BrokerStartupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

@Configuration
public class BrokerStartupConfiguration {

	@Autowired
	private Environment environment;


	@Bean
	@ConditionalOnMissingBean
	public BrokerController getBrokerController(){
		BrokerConfig brokerConfig = new BrokerConfig();
		getProperties(brokerConfig, "jmqtt.broker.");
		NettyConfig nettyConfig = new NettyConfig();
		getProperties(nettyConfig, "jmqtt.netty.");
		return new BrokerStartupService(brokerConfig, nettyConfig).getBrokerController();
	}

	private void getProperties(Object object, String prefix){
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			String key = prefix + field.getName();
			if (environment.containsProperty(key)){
				Field tempField = null;
				try {
					tempField = object.getClass().getDeclaredField(field.getName());
					tempField.setAccessible(true);
					tempField.set(object, environment.getProperty(key, field.getType()));
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
