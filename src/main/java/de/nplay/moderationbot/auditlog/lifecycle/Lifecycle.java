package de.nplay.moderationbot.auditlog.lifecycle;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lifecycle {

    private final Map<Class<? extends BotEvent>, Set<Subscriber<BotEvent>>> subscriptions = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends BotEvent> void subscribe(Class<T> event, Subscriber<T> subscriber) {
        subscriptions.computeIfAbsent(event, _ -> ConcurrentHashMap.newKeySet()).add((Subscriber<BotEvent>) subscriber);
    }

    public void publish(BotEvent event) {
        subscriptions.keySet().stream()
                .filter(it -> it.isAssignableFrom(event.getClass()))
                .map(subscriptions::get)
                .flatMap(Set::stream)
                .forEach(it -> it.accept(event));
    }
}
