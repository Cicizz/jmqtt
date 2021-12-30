
package org.jmqtt.bus;

import org.jmqtt.bus.model.DeviceMessage;

import java.util.List;

public interface DeviceMessageManager {

    void clearOfflineMessage(String clientId);

    /**
     * send message to bus
     * @param deviceMessage
     */
    void dispatcher(DeviceMessage deviceMessage);

    List<DeviceMessage> queryUnAckMessages(String clientId,int limit);

    boolean ackMessage(String clientId,Long messageId);
}
