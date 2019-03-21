package org.jmqtt.group.common;

/**
 * all cluster remoting is async,all invoke need callback
 */
public interface InvokeCallback {
    void invokeComplete(final ResponseFuture responseFuture);
}
