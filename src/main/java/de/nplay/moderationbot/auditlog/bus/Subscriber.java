package de.nplay.moderationbot.auditlog.bus;

@FunctionalInterface
public interface Subscriber<T extends BotEvent> {

    void accept(T event);

}
