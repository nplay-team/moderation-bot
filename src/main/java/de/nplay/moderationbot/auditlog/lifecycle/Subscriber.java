package de.nplay.moderationbot.auditlog.lifecycle;

public interface Subscriber<T extends BotEvent> {

    void accept(T event);

}
