package de.nplay.moderationbot.auditlog;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSubscriber implements Subscriber<BotEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoggingSubscriber.class);

    @Override
    public void accept(BotEvent event) {
        log.info("User {} performed {} on target {}", event.issuer(), event.type(), event.target());
    }
}
