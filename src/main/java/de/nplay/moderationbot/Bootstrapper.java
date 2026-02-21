package de.nplay.moderationbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrapper {

    private final static Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    static void main() {
        long startTime = System.currentTimeMillis();
        Thread.currentThread().setName("Bot");
        log.info("Starting NPLAY-Bot...");
        try {
            ModerationBot.start(System.getenv("BOT_GUILD"), System.getenv("BOT_TOKEN"));
        } catch (Exception e) {
            log.error("Failed to start!", e);
            System.exit(1);
        }
        log.info("Successfully started NPLAY-Moderation-Bot! Took {} ms", System.currentTimeMillis() - startTime);
    }
}
