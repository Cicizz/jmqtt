package org.jmqtt.broker.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JmqttLogger {

    Logger brokerlog = LoggerFactory.getLogger("brokerLog");

    Logger clientTraceLog = LoggerFactory.getLogger("clientTraceLog");

    Logger messageTraceLog = LoggerFactory.getLogger("messageTraceLog");

    Logger eventLog = LoggerFactory.getLogger("eventLog");

    Logger remotingLog = LoggerFactory.getLogger("remotingLog");

    Logger storeLog = LoggerFactory.getLogger("storeLog");

    Logger otherLog = LoggerFactory.getLogger("otherLog");

}
