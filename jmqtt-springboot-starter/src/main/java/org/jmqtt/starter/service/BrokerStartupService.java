package org.jmqtt.starter.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.config.NettyConfig;
import org.jmqtt.broker.common.helper.MixAll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class BrokerStartupService {

	private BrokerConfig brokerConfig;
	private NettyConfig nettyConfig;

	public BrokerStartupService() {
		super();
	}

	public BrokerStartupService(BrokerConfig brokerConfig,
			NettyConfig nettyConfig) {
		this.brokerConfig = brokerConfig;
		this.nettyConfig = nettyConfig;
	}

	private static void initConfig(String jmqttConfigPath, BrokerConfig brokerConfig,
			NettyConfig nettyConfig) {
		Properties properties = new Properties();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(jmqttConfigPath));
			properties.load(bufferedReader);
			MixAll.properties2POJO(properties, brokerConfig);
			MixAll.properties2POJO(properties, nettyConfig);
		} catch (FileNotFoundException e) {
			System.out.println("jmqtt.properties cannot find,cause + " + e + ",path:" + jmqttConfigPath);
		} catch (IOException e) {
			System.out.println("Handle jmqttConfig IO exception,cause = " + e);
		} finally {
			try {
				if (Objects.nonNull(bufferedReader)) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				System.out.println("Handle jmqttConfig IO exception,cause = " + e);
			}
		}
	}

	public BrokerController getBrokerController() {
		String jmqttConfigPath =
				brokerConfig.getJmqttHome() + File.separator + "conf" + File.separator + "jmqtt.properties";
		initConfig(jmqttConfigPath, brokerConfig, nettyConfig);
		try {
			LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
			File file = new File(brokerConfig.getJmqttHome() + File.separator + "conf" + File.separator + "log4j2.xml");
			context.setConfigLocation(file.toURI());
			Configuration configuration = context.getConfiguration();
			Map<String, LoggerConfig> loggerConfigMap = configuration.getLoggers();
			Level newLevel = Level.getLevel(brokerConfig.getLogLevel());
			if (newLevel == null) {
				newLevel = Level.INFO;
			}
			for (LoggerConfig value : loggerConfigMap.values()) {
				value.setLevel(newLevel);
			}
			context.updateLoggers(configuration);
		} catch (Exception ex) {
			System.err.print("Log4j2 load error,ex:" + ex);
		}
		return new BrokerController(brokerConfig, nettyConfig);
	}
}
