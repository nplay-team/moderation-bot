package de.nplay.moderationbot;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import de.nplay.moderationbot.auditlog.AuditlogService;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.permissions.PermissionsService;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.slowmode.SlowmodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DatabaseModule.class);
    private final Lifecycle lifecycle;
    private final MessageReferenceService referenceService;
    private final ModerationActService moderationActService;
    private final NotesService notesService;
    private final PermissionsService permissionsService;
    private final SlowmodeService slowmodeService;
    private final ConfigService configService;
    private final RuleService ruleService;
    private final AuditlogService auditlogService;

    public DatabaseModule() {
        initialize();
        lifecycle = new Lifecycle();
        referenceService = new MessageReferenceService();
        ruleService = new RuleService();
        moderationActService = new ModerationActService(referenceService, ruleService);
        notesService = new NotesService(lifecycle);
        permissionsService = new PermissionsService(lifecycle);
        slowmodeService = new SlowmodeService();
        configService = new ConfigService(lifecycle);
        auditlogService = new AuditlogService();
    }

    @Provides
    public Lifecycle lifecycle() {
        return lifecycle;
    }

    @Provides
    public NotesService notesService() {
        return notesService;
    }

    @Provides
    public ModerationActService moderationActService() {
        return moderationActService;
    }

    @Provides
    public PermissionsService permissionsService() {
        return permissionsService;
    }

    @Provides
    public SlowmodeService slowmodeService() {
        return slowmodeService;
    }

    @Provides
    public MessageReferenceService referenceService() {
        return referenceService;
    }

    @Provides
    public ConfigService configService() {
        return configService;
    }

    @Provides
    public RuleService ruleService() {
        return ruleService;
    }

    @Provides
    public AuditlogService auditlogService() {
        return auditlogService;
    }

    private void initialize() {
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
}
