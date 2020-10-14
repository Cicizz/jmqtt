package org.jmqtt.broker.manage;

import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.BrokerStartup;
import top.hserver.HServerApplication;


public class HttpServer {

    private BrokerController brokerController;

    public HttpServer(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    public void start() {
        if (brokerController.getNettyConfig().getStartHttp()) {
            HServerApplication.run(BrokerStartup.class, brokerController.getNettyConfig().getHttpPort());
        }
    }
}
