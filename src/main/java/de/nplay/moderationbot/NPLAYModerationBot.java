package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.JsonErrorMessageFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Objects;
import java.util.Set;

/**
 * The main class of the bot
 */
public class NPLAYModerationBot {

    private final JDA jda;
    private final JDACommands jdaCommands;
    private final Guild guild;
    private final EmbedCache embedCache;

    /**
     * Constructor of the bot, creates a JDA instance and initiates all relevant services.
     *
     * @param guildId The guild the bot should listen to
     * @param token   The discord bot token
     */
    private NPLAYModerationBot(String guildId, String token) throws InterruptedException {
        jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .setActivity(Activity.customStatus("NPLAY Moderation - Booting..."))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build().awaitReady();

        guild = Objects.requireNonNull(jda.getGuildById(guildId), "Failed to load guild");

        jdaCommands = JDACommands.start(jda, NPLAYModerationBot.class, "de.nplay.moderationbot");
        embedCache = new EmbedCache("embeds.json");
        jdaCommands.getDependencyInjector().registerProvider(this);
        jdaCommands.getImplementationRegistry().setErrorMessageFactory(new JsonErrorMessageFactory(embedCache));
        jdaCommands.getImplementationRegistry().setGuildScopeProvider(commandData -> Set.of(guild.getIdLong()));

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("euren Nachrichten"), false);
    }

    /**
     * Creates and starts a new Bot instance
     *
     * @param guildId The guild the bot should listen to
     * @param token   The discord bot token
     * @return The {@link NPLAYModerationBot} instance
     */
    public static NPLAYModerationBot start(String guildId, String token) throws InterruptedException {
        return new NPLAYModerationBot(guildId, token);
    }

    /**
     * Shuts the bot and all relevant services down.
     */
    public void shutdown() {
        jda.shutdown();
    }

    public JDA getJda() {
        return jda;
    }

    public JDACommands getJdaCommands() {
        return jdaCommands;
    }

    public Guild getGuild() {
        return guild;
    }

    @Produces(skipIndexing = true)
    public EmbedCache getEmbedCache() {
        return embedCache;
    }
}
