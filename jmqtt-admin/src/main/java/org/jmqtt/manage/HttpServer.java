package org.jmqtt.manage;

import top.hserver.HServerApplication;

public class HttpServer {

    private int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        port = port != 0 ? port : 8080;
        HServerApplication.run(HttpServer.class, port);
    }
}
