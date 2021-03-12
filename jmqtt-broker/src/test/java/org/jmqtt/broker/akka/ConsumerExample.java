package org.jmqtt.broker.akka;

import org.jmqtt.broker.processor.dispatcher.event.Event;

public class ConsumerExample {

    public ConsumerExample(){

    }

    public void consume(Event event) {
        System.out.println("consume event = " + event);
    }

}
