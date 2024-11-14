package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.JsonErrorMessageFactory;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/**
 * The main class of the bot
 */
public class NPLAYModerationBot {

    private static final Logger log = LoggerFactory.getLogger(NPLAYModerationBot.class);
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

        var dataSource = DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config.host(System.getenv("POSTGRES_HOST"))
                        .port(System.getenv("POSTGRES_PORT"))
                        .user(System.getenv("POSTGRES_USER"))
                        .password(System.getenv("POSTGRES_PASSWORD"))
                        .database(System.getenv("POSTGRES_DATABASE"))
                )
                .create()
                .build();

        var config = QueryConfiguration.builder(dataSource)
                .setExceptionHandler(err -> log.error("An error occurred during a database request", err))
                .setRowMapperRegistry(new RowMapperRegistry().register(PostgresqlMapper.getDefaultMapper()))
                .build();
        QueryConfiguration.setDefault(config);

        try {
            SqlUpdater.builder(dataSource, PostgreSql.get()).execute();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to migrate database!", e);
        }
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