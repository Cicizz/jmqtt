package org.jmqtt.support.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JmqttLogger {

    Logger brokerLog = LoggerFactory.getLogger("brokerLog");

    Logger mqttLog = LoggerFactory.getLogger("mqttLog");

    Logger busLog = LoggerFactory.getLogger("busLog");

    Logger messageTraceLog = LoggerFactory.getLogger("messageTraceLog");

    Logger remotingLog = LoggerFactory.getLogger("remotingLog");

    Logger storeLog = LoggerFactory.getLogger("storeLog");

    Logger otherLog = LoggerFactory.getLogger("otherLog");

    Logger monitorLog = LoggerFactory.getLogger("monitorLog");

}
