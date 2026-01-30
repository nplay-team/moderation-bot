package de.nplay.moderationbot.auditlog.event;


public final class Subscription {

    private final Class<? extends BotEvent> event;
    private final Subscriber<?> subscriber;
    private final Lifecycle lifecycle;

    public Subscription(Class<? extends BotEvent> event, Subscriber<?> subscriber, Lifecycle lifecycle) {
        this.event = event;
        this.subscriber = subscriber;
        this.lifecycle = lifecycle;
    }

    public void unsubscribe() {
        lifecycle.unsubscribe(subscriber, event);
    }
}
