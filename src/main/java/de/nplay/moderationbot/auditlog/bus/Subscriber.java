package de.nplay.moderationbot.auditlog.bus;

public interface Subscriber<T extends BotEvent> {

    void accept(T event);

}
