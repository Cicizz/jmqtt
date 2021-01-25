package org.jmqtt.broker;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.config.NettyConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * jmqtt 启动类,代码风格参考RocketMQ
 * 代码阅读沟通，二次开发文档：请加 qq群：578185385 或 http://www.mangdagou.com/
 *
 */
public class BrokerStartup {

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception e) {
            System.out.println("Jmqtt start failure,cause = " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static BrokerController start(String[] args) throws Exception {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options,args);
        String jmqttHome = null;
        String jmqttConfigPath = null;
        BrokerConfig brokerConfig = new BrokerConfig();
        NettyConfig nettyConfig = new NettyConfig();
        if(commandLine != null){
            jmqttHome = commandLine.getOptionValue("h");
            jmqttConfigPath = commandLine.getOptionValue("c");
        }
        if(StringUtils.isEmpty(jmqttHome)){
            jmqttHome = brokerConfig.getJmqttHome();
        }
        if(StringUtils.isEmpty(jmqttHome)){
            throw new Exception("please set JMQTT_HOME.");
        }
        if(StringUtils.isEmpty(jmqttConfigPath)){
            jmqttConfigPath = jmqttHome + File.separator + "conf" + File.separator + "jmqtt.properties";
        }
        initConfig(jmqttConfigPath,brokerConfig,nettyConfig);

        // 日志配置加载
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(jmqttHome + "/conf/logback_broker.xml");

        // 启动服务，线程等
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
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c",true,"jmqtt.properties path,eg: /wls/xxx/xxx.properties");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    /**
     * convert properties to java config class
     * @param jmqttConfigPath
     * @param brokerConfig
     * @param nettyConfig
     */
    private static void initConfig(String jmqttConfigPath, BrokerConfig brokerConfig, NettyConfig nettyConfig){
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
                if(Objects.nonNull(bufferedReader)){
                    bufferedReader.close();
                }
            } catch (IOException e) {
                System.out.println("Handle jmqttConfig IO exception,cause = " + e);
            }
        }
    }

}
