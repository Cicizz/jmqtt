package org.jmqtt.mqtt.retain;

import org.jmqtt.bus.model.DeviceMessage;

import java.util.List;

public interface RetainMessageHandler {

    List<DeviceMessage> getAllRetatinMessage();

    void clearRetainMessage(String topic);

    void storeRetainMessage(DeviceMessage deviceMessage);
}
