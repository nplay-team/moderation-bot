package de.nplay.moderationbot.auditlog.lifecycle;

import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lifecycle {

    private final Map<Class<? extends BotEvent>, Set<Subscriber<BotEvent>>> subscriptions = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends BotEvent> Subscription subscribe(Class<T> event, Subscriber<T> subscriber) {
        subscriptions.computeIfAbsent(event, _ -> ConcurrentHashMap.newKeySet()).add((Subscriber<@NonNull BotEvent>) subscriber);
        return new Subscription(event, subscriber, this);
    }

    public void unsubscribe(Subscriber<?> subscriber, Class<? extends BotEvent> eventType) {
        subscriptions.get(eventType).remove(subscriber);
    }

    public void publish(BotEvent event) {
        for (Subscriber<BotEvent> subscriber : subscriptions.getOrDefault(event.getClass(), Set.of())) {
            subscriber.accept(event);
        }
    }
}
