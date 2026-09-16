package de.nplay.moderationbot.auditlog.bus;

public class EventBusService {

    private final EventBus eventBus;

    public EventBusService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void publish(BotEvent event) {
        eventBus.publish(event);
    }

}
