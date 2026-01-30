package de.nplay.moderationbot.auditlog.lifecycle;

public class Service {

    private final Lifecycle lifecycle;

    public Service(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void publish(BotEvent event) {
        lifecycle.publish(event);
    }

}
