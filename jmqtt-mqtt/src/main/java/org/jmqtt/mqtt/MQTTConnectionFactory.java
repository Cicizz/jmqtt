
package org.jmqtt.mqtt;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.jmqtt.bus.BusController;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.mqtt.protocol.impl.*;
import org.jmqtt.mqtt.retain.RetainMessageHandler;
import org.jmqtt.mqtt.retain.impl.RetainMessageHandlerImpl;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.helper.Pair;
import org.jmqtt.support.helper.RejectHandler;
import org.jmqtt.support.helper.ThreadFactoryImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * mqtt connection factory
 */
public class MQTTConnectionFactory {

    private BrokerConfig brokerConfig;

    /** mqtt protocol processor */
    private ExecutorService connectExecutor;
    private  ExecutorService pubExecutor;
    private  ExecutorService subExecutor;
    private  ExecutorService pingExecutor;
    private     LinkedBlockingQueue<Runnable>                     connectQueue;
    private     LinkedBlockingQueue<Runnable>                     pubQueue;
    private     LinkedBlockingQueue<Runnable>                     subQueue;
    private     LinkedBlockingQueue<Runnable>                     pingQueue;
    private Map<Integer, Pair<RequestProcessor, ExecutorService>> processorTable;

    private RetainMessageHandler retainMessageHandler;

    /** bus dependency */
    private BusController busController;



    public MQTTConnectionFactory(BrokerConfig brokerConfig, BusController busController) {

        this.brokerConfig = brokerConfig;
        this.busController = busController;
        this.retainMessageHandler = new RetainMessageHandlerImpl();

        this.connectQueue = new LinkedBlockingQueue<>(100000);
        this.pubQueue = new LinkedBlockingQueue<>(100000);
        this.subQueue = new LinkedBlockingQueue<>(100000);
        this.pingQueue = new LinkedBlockingQueue<>(10000);

        int coreThreadNum = Runtime.getRuntime().availableProcessors();
        this.connectExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                connectQueue,
                new ThreadFactoryImpl("ConnectThread"),
                new RejectHandler("connect", 100000));
        this.pubExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                pubQueue,
                new ThreadFactoryImpl("PubThread"),
                new RejectHandler("pub", 100000));
        this.subExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                subQueue,
                new ThreadFactoryImpl("SubThread"),
                new RejectHandler("sub", 100000));
        this.pingExecutor = new ThreadPoolExecutor(coreThreadNum,
                coreThreadNum,
                60000,
                TimeUnit.MILLISECONDS,
                pingQueue,
                new ThreadFactoryImpl("PingThread"),
                new RejectHandler("heartbeat", 100000));

        RequestProcessor connectProcessor = new ConnectProcessor();
        RequestProcessor disconnectProcessor = new DisconnectProcessor();
        RequestProcessor pingProcessor = new PingProcessor();
        RequestProcessor publishProcessor = new PublishProcessor();
        RequestProcessor pubRelProcessor = new PubRelProcessor();
        RequestProcessor subscribeProcessor = new SubscribeProcessor();
        RequestProcessor unSubscribeProcessor = new UnSubscribeProcessor();
        RequestProcessor pubRecProcessor = new PubRecProcessor();
        RequestProcessor pubAckProcessor = new PubAckProcessor();
        RequestProcessor pubCompProcessor = new PubCompProcessor();

        processorTable = new HashMap<>();
        registerProcessor(MqttMessageType.CONNECT.value(), connectProcessor, connectExecutor);
        registerProcessor(MqttMessageType.DISCONNECT.value(), disconnectProcessor,
                connectExecutor);
        registerProcessor(MqttMessageType.PINGREQ.value(), pingProcessor, pingExecutor);
        registerProcessor(MqttMessageType.PUBLISH.value(), publishProcessor, pubExecutor);
        registerProcessor(MqttMessageType.PUBACK.value(), pubAckProcessor, pubExecutor);
        registerProcessor(MqttMessageType.PUBREL.value(), pubRelProcessor, pubExecutor);
        registerProcessor(MqttMessageType.SUBSCRIBE.value(), subscribeProcessor, subExecutor);
        registerProcessor(MqttMessageType.UNSUBSCRIBE.value(), unSubscribeProcessor, subExecutor);
        registerProcessor(MqttMessageType.PUBREC.value(), pubRecProcessor, subExecutor);
        registerProcessor(MqttMessageType.PUBCOMP.value(), pubCompProcessor, subExecutor);
    }

    public void registerProcessor(int mqttMessageType, RequestProcessor requestProcessor, ExecutorService executorService){
        processorTable.put(mqttMessageType,new Pair<>(requestProcessor,executorService));
    }

    public MQTTConnection create(Channel channel){
        MQTTConnection mqttConnection = new MQTTConnection(channel,processorTable,brokerConfig,busController,retainMessageHandler);
        return mqttConnection;
    }


    public void shutdown(){
        this.connectExecutor.shutdown();
        this.pubExecutor.shutdown();
        this.subExecutor.shutdown();
        this.pingExecutor.shutdown();
    }

}
