package org.jmqtt.store;


import org.jmqtt.common.model.Message;

import java.util.Collection;

/**
 * cleansession message
 */
public interface OfflineMessageStore {

    void clearOfflineMsgCache(String clientId);

    boolean containOfflineMsg(String clientId);

    boolean addOfflineMessage(String clientId, Message message);

    Collection<Message> getAllOfflineMessage(String clientId);


}
