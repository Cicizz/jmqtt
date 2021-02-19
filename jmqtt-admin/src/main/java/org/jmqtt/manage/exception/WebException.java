package org.jmqtt.manage.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import top.hserver.core.interfaces.GlobalException;
import top.hserver.core.ioc.annotation.Bean;
import top.hserver.core.server.context.Webkit;

import java.io.File;

@Bean
public class WebException implements GlobalException {
    @Override
    public void handler(Throwable throwable, int httpStatusCode, String errorDescription, Webkit webkit) {
        webkit.httpResponse.sendStatusCode(HttpResponseStatus.NOT_FOUND);
        webkit.httpResponse.sendTemplate("error" + File.separator + "404.ftl");
    }
}