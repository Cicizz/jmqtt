
package org.jmqtt.bus;

import org.jmqtt.bus.enums.MessageAckEnum;
import org.jmqtt.bus.model.DeviceMessage;

import java.util.List;

public interface DeviceMessageManager {

    void clearUnAckMessage(String clientId);

    /**
     * send message to bus
     * @param deviceMessage
     */
    void dispatcher(DeviceMessage deviceMessage);

    Long storeMessage(DeviceMessage deviceMessage);

    List<DeviceMessage> queryUnAckMessages(String clientId,int limit);

    List<DeviceMessage> queryByIds(List<Long> ids);

    Long addClientInBoxMsg(String clientId,Long messageId, MessageAckEnum ackEnum);

    boolean ackMessage(String clientId,Long messageId);
}
