package de.nplay.moderationbot.auditlog.lifecycle;

public class LifecycleService {

    private final Lifecycle lifecycle;

    public LifecycleService(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void publish(BotEvent event) {
        lifecycle.publish(event);
    }

}
