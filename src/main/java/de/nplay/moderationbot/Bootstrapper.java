package de.nplay.moderationbot;

import de.nplay.moderationbot.tasks.AutomaticUnbanTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bootstrapper {

    private final static Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    /**
     * Main entry point of the Bot
     */
    public static void main(String[] args) {
        Thread.currentThread().setName("Bot");
        long startTime = System.currentTimeMillis();
        try {
            log.info("Starting NPLAY-Bot...");
            NPLAYModerationBot bot = NPLAYModerationBot.start(System.getenv("BOT_GUILD"), System.getenv("BOT_TOKEN"));
            Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));
            Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
            log.info("Successfully started NPLAY-Moderation-Bot! Took {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to start!", e);
            System.exit(1);
        }
    }
}
