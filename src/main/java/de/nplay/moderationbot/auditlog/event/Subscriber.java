package de.nplay.moderationbot.auditlog.event;

public interface Subscriber<T extends BotEvent> {

    void accept(T event);

}
