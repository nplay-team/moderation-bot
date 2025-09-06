package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.definitions.interactions.command.CommandDefinition.CommandConfig;
import com.github.kaktushose.jda.commands.embeds.EmbedDataSource;
import com.github.kaktushose.jda.commands.guice.GuiceExtensionData;
import com.github.kaktushose.jda.commands.i18n.FluavaLocalizer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.serverlog.Serverlog;
import de.nplay.moderationbot.slowmode.SlowmodeEventHandler;
import dev.goldmensch.fluava.Fluava;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class NPLAYModerationBot extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NPLAYModerationBot.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final JDA jda;
    private final JDACommands jdaCommands;
    private final Guild guild;
    private final Serverlog serverlog;
    private final ModerationActLock moderationActLock = new ModerationActLock();

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
                .setEventPool(Executors.newVirtualThreadPerTaskExecutor())
                .build().awaitReady();

        guild = Objects.requireNonNull(jda.getGuildById(guildId), "Failed to load guild");

        serverlog = new Serverlog();

        jdaCommands = JDACommands.builder(jda, NPLAYModerationBot.class, "de.nplay.moderationbot")
                .embeds(config -> config
                        .sources(EmbedDataSource.resource("embeds.json"))
                        .errorSource(EmbedDataSource.resource("embeds.json"))
                        .placeholders(
                                entry("colorDefault", EmbedColors.DEFAULT),
                                entry("colorSuccess", EmbedColors.SUCCESS),
                                entry("colorWarning", EmbedColors.WARNING),
                                entry("colorError", EmbedColors.ERROR)
                        )
                )
                .localizer(new FluavaLocalizer(new Fluava(Locale.GERMAN)))
                .globalCommandConfig(CommandConfig.of(config -> config.enabledPermissions(Permission.MODERATE_MEMBERS)))
                .extensionData(new GuiceExtensionData(Guice.createInjector(this)))
                .start();

        jda.addEventListener(new SlowmodeEventHandler(jdaCommands::embed));

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

        scheduler.scheduleAtFixedRate(
                () -> ModerationActService.getModerationActsToRevert().forEach(it ->
                        it.revert(guild, jdaCommands::embed, jda.getSelfUser(), "Automatische Aufhebung nach Ablauf der Dauer")
                ), 0, 1, TimeUnit.MINUTES);
    }

    public static NPLAYModerationBot start(String guildId, String token) throws InterruptedException {
        return new NPLAYModerationBot(guildId, token);
    }

    public void shutdown() {
        jda.shutdown();
    }

    @Provides
    public Serverlog serverlog() {
        return serverlog;
    }

    @Provides
    public ModerationActLock moderationActLock() {
        return moderationActLock;
    }

    public enum EmbedColors {
        DEFAULT("134180"),
        ERROR("16711680"),
        SUCCESS("65280"),
        WARNING("16776960");

        public final String color;

        EmbedColors(String color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return color;
        }
    }
}
