package org.jmqtt.support.helper;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectHandler implements RejectedExecutionHandler {
    private String task;
    private int maxBlockQueueSize;

    public RejectHandler(String task,int maxBlockQueueSize){
        this.task = task;
        this.maxBlockQueueSize = maxBlockQueueSize;
    };

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException("Task:" + task + ",maxBlockQueueSize:" + maxBlockQueueSize
                + ",Thread:" + r.toString());
    }
}
