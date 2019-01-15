package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;

import java.util.Collection;


public interface RedisDao {
    void delete(String clientId);
    Message hgetMsg(String clientId,Integer msgId);
    boolean hstoreMsg(String clientId, String str, Object obj);
    Message hreleaseMsg(String clientId,int msgId);
    <T> Collection<T> hgetAllMsg(String clientId,Class objectClass);
    boolean hcontainMsg(String clientId,int msgId);
    Collection<Message> sgetAllMsg();
    void sstoreMsg(String str,Message message);
    boolean scontain(String str);
    <T> T sgetSetMsg(String str,Object obj);
    <T> T sgetMsg(String str,Class objectClass);
    Collection<Message> lgetAllMsg(String str,Integer num);
    boolean laddMsg(Integer num,String str,Message message);
}
