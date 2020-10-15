package org.jmqtt.manage;

import org.jmqtt.common.config.NettyConfig;
import top.hserver.HServerApplication;


public class HttpServer {

    private NettyConfig nettyConfig;

    public HttpServer(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    public void start() {
        if (this.nettyConfig.getStartHttp()) {
            HServerApplication.run(HttpServer.class, this.nettyConfig.getHttpPort());
        }
    }
}
