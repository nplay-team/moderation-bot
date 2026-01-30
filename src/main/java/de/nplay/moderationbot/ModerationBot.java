package de.nplay.moderationbot;

import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.definitions.interactions.InteractionDefinition.ReplyConfig;
import io.github.kaktushose.jdac.definitions.interactions.command.CommandDefinition.CommandConfig;
import io.github.kaktushose.jdac.embeds.EmbedDataSource;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import io.github.kaktushose.jdac.message.i18n.FluavaLocalizer;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import com.google.inject.Guice;
import com.google.inject.Provides;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.moderation.lock.ModerationActLock;
import de.nplay.moderationbot.serverlog.Serverlog;
import de.nplay.moderationbot.slowmode.SlowmodeEventHandler;
import dev.goldmensch.fluava.Fluava;
import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.Result.Success;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;
import static net.dv8tion.jda.api.utils.TimeFormat.*;

public class ModerationBot extends ServiceModule {

    private static final Logger log = LoggerFactory.getLogger(ModerationBot.class);
    private final JDA jda;
    private final Guild guild;
    private final Serverlog serverlog;
    private final ModerationActLock moderationActLock = new ModerationActLock();

    private ModerationBot(String guildId, String token) throws InterruptedException {
        database();

        jda = jda(token);
        guild = Objects.requireNonNull(jda.getGuildById(guildId), "Failed to load guild");
        serverlog = new Serverlog();

        JDACommands jdaCommands = jdaCommands(fluava());
        MessageResolver resolver = jdaCommands.property(Property.MESSAGE_RESOLVER);
        jda.addEventListener(new SlowmodeEventHandler(resolver));

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
                () -> moderationActService().automaticRevert(guild, resolver),
                0, 1, TimeUnit.MINUTES
        );

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));

        jda.getPresence().setPresence(
                OnlineStatus.ONLINE,
                Activity.listening(resolver.resolve("bot-status", Locale.GERMAN)),
                false
        );
    }

    public static void start(String guildId, String token) throws InterruptedException {
        new ModerationBot(guildId, token);
    }

    @Provides
    public Serverlog serverlog() {
        return serverlog;
    }

    @Provides
    public ModerationActLock moderationActLock() {
        return moderationActLock;
    }

    private JDA jda(String token) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token)
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

        Runtime.getRuntime().addShutdownHook(new Thread(jda::shutdown));

        return jda;
    }

    private Fluava fluava() {
        return Fluava.builder()
                .fallback(Locale.GERMAN)
                .bundleRoot("localization")
                .functions(config ->
                        config.register("RESOLVED_USER", Function.implicit((_, user, _) ->
                                result(formatUser(jda, user)), UserSnowflake.class)
                        ).register("RELATIVE_TIME", Function.implicit((_, time, _) ->
                                result("%s (%s)".formatted(DATE_TIME_LONG.format(time.millis()), RELATIVE.atTimestamp(time.millis()))), RelativeTime.class)
                        ).register("ABSOLUTE_TIME", Function.implicit((_, time, _) ->
                                result(DATE_TIME_SHORT.format(time.millis())), AbsoluteTime.class)
                        ).register("RESOLVED_CHANNEL", Function.implicit((_, channel, _) ->
                                result(channel.getAsMention()), Channel.class))
                ).build();
    }

    // TODO workaround until Fluava improves API
    private Result<Text> result(String value) {
        return new Success<>(new Text(value));
    }

    private JDACommands jdaCommands(Fluava parent) {
        return JDACommands.builder(jda)
                .packages("de.nplay.moderationbot")
                .embeds(config -> config.sources(EmbedDataSource.resource("events.json"))
                        .placeholders(
                                entry("colorDefault", Color.decode(EmbedColors.DEFAULT.hex)),
                                entry("colorSuccess", Color.decode(EmbedColors.SUCCESS.hex)),
                                entry("colorWarning", Color.decode(EmbedColors.WARNING.hex)),
                                entry("colorError", Color.decode(EmbedColors.ERROR.hex))
                        )
                ).localizer(new FluavaLocalizer(parent))
                .globalReplyConfig(ReplyConfig.of(config -> config.allowedMentions(List.of())
                        .keepComponents(false))
                ).globalCommandConfig(CommandConfig.of(config -> config
                        .enabledPermissions(Permission.MODERATE_MEMBERS)
                        .integration(IntegrationType.GUILD_INSTALL)
                        .context(InteractionContextType.GUILD))
                ).extensionData(new GuiceExtensionData(Guice.createInjector(this)))
                .start();
    }

    private void database() {
        var dataSource = DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config.host(System.getenv("POSTGRES_HOST"))
                        .port(System.getenv("POSTGRES_PORT"))
                        .user(System.getenv("POSTGRES_USER"))
                        .password(System.getenv("POSTGRES_PASSWORD"))
                        .database(System.getenv("POSTGRES_DATABASE"))
                ).create()
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

    private String formatUser(JDA jda, UserSnowflake user) {
        if (user instanceof User resolved) {
            return "%s (%s)".formatted(resolved.getAsMention(), resolved.getEffectiveName());
        }
        return "%s (%s)".formatted(user.getAsMention(), jda.retrieveUserById(user.getId()).complete().getEffectiveName());
    }

    @Deprecated
    public enum EmbedColors {
        DEFAULT("#020C24"),
        ERROR("#FF0000"),
        SUCCESS("#00FF00"),
        WARNING("#FFFF00");

        public final String hex;

        EmbedColors(String hex) {
            this.hex = hex;
        }

        @Override
        public String toString() {
            return hex;
        }
    }
}
