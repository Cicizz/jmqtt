
package org.jmqtt.bus;

import org.jmqtt.bus.model.DeviceSession;

public interface DeviceSessionManager {



    DeviceSession getSession(String clientId);

    void storeSession(DeviceSession deviceSession);


    void offline(String clientId);
}
