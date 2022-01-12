package org.jmqtt.bus.subscription;

import java.util.concurrent.atomic.AtomicReference;

public class INode {
    private AtomicReference<CNode> mainNode = new AtomicReference<>();

    INode(CNode mainNode) {
        this.mainNode.set(mainNode);
        if (mainNode instanceof TNode) { // this should never happen
            throw new IllegalStateException("TNode should not be set on mainNnode");
        }
    }

    boolean compareAndSet(CNode old, CNode newNode) {
        return mainNode.compareAndSet(old, newNode);
    }

    boolean compareAndSet(CNode old, TNode newNode) {
        return mainNode.compareAndSet(old, newNode);
    }

    CNode mainNode() {
        return this.mainNode.get();
    }

    boolean isTombed() {
        return this.mainNode() instanceof TNode;
    }
}
