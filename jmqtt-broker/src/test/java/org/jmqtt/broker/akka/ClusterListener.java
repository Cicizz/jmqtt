package org.jmqtt.broker.akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;
import org.jmqtt.broker.akka.ClusterListener.Event;

public final class ClusterListener extends AbstractBehavior<Event> {

    private ClusterListener(ActorContext<Event> context) {
        super(context);

        Cluster cluster = Cluster.get(context.getSystem());

        ActorRef<MemberEvent> memberEventAdapter =
            context.messageAdapter(ClusterEvent.MemberEvent.class, MemberChange::new);
        cluster.subscriptions()
            .tell(Subscribe.create(memberEventAdapter, ClusterEvent.MemberEvent.class));

        ActorRef<ClusterEvent.ReachabilityEvent> reachabilityAdapter =
            context.messageAdapter(ClusterEvent.ReachabilityEvent.class, ReachabilityChange::new);
        cluster.subscriptions()
            .tell(Subscribe.create(reachabilityAdapter, ClusterEvent.ReachabilityEvent.class));
    }

    public static Behavior<Event> create() {
        return Behaviors.setup(ClusterListener::new);
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder()
            .onMessage(ReachabilityChange.class, this::onReachabilityChange)
            .onMessage(MemberChange.class, this::onMemberChange)
            .build();
    }

    private Behavior<Event> onReachabilityChange(ReachabilityChange event) {
        if (event.reachabilityEvent instanceof ClusterEvent.UnreachableMember) {
            getContext().getLog()
                .info("Member detected as unreachable: {}", event.reachabilityEvent.member());
        } else if (event.reachabilityEvent instanceof ClusterEvent.ReachableMember) {
            getContext().getLog()
                .info("Member back to reachable: {}", event.reachabilityEvent.member());
        }
        return this;
    }

    private Behavior<Event> onMemberChange(MemberChange event) {
        if (event.memberEvent instanceof ClusterEvent.MemberUp) {
            getContext().getLog().info("Member is up: {}", event.memberEvent.member());
        } else if (event.memberEvent instanceof ClusterEvent.MemberRemoved) {
            getContext().getLog().info("Member is removed: {} after {}",
                event.memberEvent.member(),
                ((ClusterEvent.MemberRemoved) event.memberEvent).previousStatus()
            );
        }
        return this;
    }


    interface Event {

    }

    // internal adapted cluster events only
    private static final class ReachabilityChange implements Event {

        final ClusterEvent.ReachabilityEvent reachabilityEvent;

        ReachabilityChange(ClusterEvent.ReachabilityEvent reachabilityEvent) {
            this.reachabilityEvent = reachabilityEvent;
        }
    }

    private static final class MemberChange implements Event {

        final ClusterEvent.MemberEvent memberEvent;

        MemberChange(ClusterEvent.MemberEvent memberEvent) {
            this.memberEvent = memberEvent;
        }
    }

}

