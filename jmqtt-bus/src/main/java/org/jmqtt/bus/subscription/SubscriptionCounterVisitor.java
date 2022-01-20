package org.jmqtt.bus.subscription;

import java.util.concurrent.atomic.AtomicInteger;

class SubscriptionCounterVisitor implements CTrie.IVisitor<Integer> {

    private AtomicInteger accumulator = new AtomicInteger(0);

    @Override
    public void visit(CNode node, int deep) {
        accumulator.addAndGet(node.subscriptions.size());
    }

    @Override
    public Integer getResult() {
        return accumulator.get();
    }
}
