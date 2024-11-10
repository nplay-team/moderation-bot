package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * The main class of the bot
 */
public class NPLAYModerationBot {

    private final JDA api;
    private final JDACommands commands;
    private final Guild guild;
    private final Database database;

    /**
     * Constructor of the bot, creates a JDA instance and initiates all relevant services.
     *
     * @param guildId The guild the bot should listen to
     * @param token   The discord bot token
     */
    private NPLAYModerationBot(long guildId, String token) {
        api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .setActivity(Activity.customStatus("NPLAY Moderation - Booting..."))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();

        database = new Database(this);
        
        commands = JDACommands.start(api, NPLAYModerationBot.class, "de.nplay.moderationbot");
        commands.getDependencyInjector().registerProvider(this);

        guild = api.getGuildById(guildId);

        api.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("euren Nachrichten"), false);
    }

    /**
     * Creates and starts a new Bot instance
     *
     * @param guildId The guild the bot should listen to
     * @param token   The discord bot token
     * @return The {@link NPLAYModerationBot} instance
     */
    public static NPLAYModerationBot start(long guildId, String token) {
        return new NPLAYModerationBot(guildId, token);
    }

    /**
     * Shuts the bot and all relevant services down.
     */
    public void shutdown() {
        api.shutdown();
    }

    public JDA getApi() {
        return api;
    }

    public JDACommands getCommands() {
        return commands;
    }

    public Guild getGuild() {
        return guild;
    }
    
    @Produces(skipIndexing = true)
    public Database getDatabase() {
        return database;
    }
}
