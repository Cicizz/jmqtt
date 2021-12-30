package org.jmqtt.support.helper;

public class Pair<T,K> {
    private T object1;
    private K object2;

    public Pair(T object1, K object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    public T getObject1() {
        return object1;
    }

    public void setObject1(T object1) {
        this.object1 = object1;
    }

    public K getObject2() {
        return object2;
    }

    public void setObject2(K object2) {
        this.object2 = object2;
    }
}
