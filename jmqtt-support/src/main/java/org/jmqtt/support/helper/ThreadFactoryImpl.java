package org.jmqtt.support.helper;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryImpl implements ThreadFactory {

    private AtomicInteger counter = new AtomicInteger(0);
    private String threadName;

    public ThreadFactoryImpl(String threadName){
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,threadName + "_" + this.counter.incrementAndGet());
    }
}
