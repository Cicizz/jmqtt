package org.jmqtt.mqtt.retain.impl;

import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.mqtt.retain.RetainMessageHandler;

import java.util.List;

public class RetainMessageHandlerImpl implements RetainMessageHandler {


    @Override
    public List<DeviceMessage> getAllRetatinMessage() {
        return null;
    }

    @Override
    public void clearRetainMessage(String topic) {

    }

    @Override
    public void storeRetainMessage(DeviceMessage deviceMessage) {

    }
}
