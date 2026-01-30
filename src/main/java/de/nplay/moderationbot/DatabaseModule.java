package de.nplay.moderationbot;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.SqlUpdater;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.notes.NotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DatabaseModule.class);
    private final ModerationActService moderationActService;
    private final NotesService notesService;

    public DatabaseModule() {
        initialize();
        notesService = new NotesService();
        moderationActService = new ModerationActService();
    }

    @Provides
    public NotesService notesService() {
        return notesService;
    }

    @Provides
    public ModerationActService moderationActService() {
        return moderationActService;
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
