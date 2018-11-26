package org.jmqtt.broker;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.exception.BrokerException;
import org.jmqtt.common.config.BrokerConfig;
import org.jmqtt.common.config.NettyConfig;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class BrokerStartup {

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception e) {
            System.out.println("Jmqtt start failure,cause = " + e);
            System.exit(-1);
        }
    }

    public static BrokerController start(String[] args) throws Exception {

        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options,args);
        if(commandLine == null){
            throw new BrokerException("mqttHome or jmqttConfigPath cannot be none");
        }
        String jmqttHome = commandLine.getOptionValue("h");
        String jmqttConfigPath = commandLine.getOptionValue("c");
        if(StringUtils.isAnyEmpty(jmqttHome,jmqttConfigPath)){
            throw new BrokerException("mqttHome or jmqttConfigPath cannot be none");
        }
        BrokerConfig brokerConfig = new BrokerConfig();
        NettyConfig nettyConfig = new NettyConfig();
        initConfig(jmqttConfigPath,brokerConfig,nettyConfig);
        if(StringUtils.isEmpty(jmqttHome)){
            jmqttHome = brokerConfig.getJmqttHome();
        }
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(jmqttHome + "/conf/logback_broker.xml");

        BrokerController brokerController = new BrokerController(brokerConfig,nettyConfig);
        brokerController.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                brokerController.shutdown();
            }
        }));

        return brokerController;
    }

    private static Options buildOptions(){
        Options options = new Options();
        Option opt = new Option("h",true,"jmqttHome,eg: /wls/xxx");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c",true,"jmqtt.properties path,eg: /wls/xxx/xxx.properties");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    private static void initConfig(String jmqttConfigPath,BrokerConfig brokerConfig,NettyConfig nettyConfig){
        Properties properties = new Properties();
        BufferedReader  bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(jmqttConfigPath));
            properties.load(bufferedReader);
            MixAll.properties2POJO(properties,brokerConfig);
            MixAll.properties2POJO(properties,nettyConfig);
        } catch (FileNotFoundException e) {
            System.out.println("jmqtt.properties cannot find,cause = " + e);
        } catch (IOException e) {
            System.out.println("Handle jmqttConfig IO exception,cause = " + e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Handle jmqttConfig IO exception,cause = " + e);
            }
        }
    }

}
