package org.jmqtt.manage.controller;

import top.hserver.core.interfaces.HttpResponse;
import top.hserver.core.ioc.annotation.Controller;
import top.hserver.core.ioc.annotation.GET;

@Controller
public class IndexController {
    @GET("/")
    public void index(HttpResponse response) {
        response.sendTemplate("index.ftl");
    }
}
