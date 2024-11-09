package de.nplay.moderationbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            
            String token = System.getenv("BOT_TOKEN");
            String guildId = System.getenv("BOT_GUILD");
            
            if(token == null || guildId == null) {
                throw new RuntimeException("Error starting bot, missing BOT_TOKEN and/or BOT_GUILD.");
            }

            var bot = NPLAYModerationBot.start(Long.parseLong(guildId), token);
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("An uncaught exception has occurred!", e));
            Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));

            log.info("Successfully started NPLAY-Moderation-Bot! Took {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to start!", e);
            System.exit(1);
        }
    }
}
