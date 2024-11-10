package de.nplay.moderationbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * The database class, responsible for the connection to the database
 */
public class Database {
    private final HikariDataSource dataSource;
    
    /**
     * Constructor of the database, creates a HikariDataSource instance
     * @param bot The bot instance
     * @throws RuntimeException If the POSTGRES_URL environment variable is missing
     */
    public Database(NPLAYModerationBot bot) throws RuntimeException {
        var config = new HikariConfig();
        var jdbcUrl = System.getenv("POSTGRES_URL");
        
        if(jdbcUrl == null) {
            throw new RuntimeException("Error starting bot, missing POSTGRES_URL.");
        }
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(System.getenv("POSTGRES_USER"));
        config.setPassword(System.getenv("POSTGRES_PASSWORD"));
        config.addDataSourceProperty("databaseName", System.getenv("POSTGRES_DATABASE"));
        
        dataSource = new HikariDataSource(config);
    }

    /**
     * Returns the {@link HikariDataSource} instance
     * @return The {@link HikariDataSource} instance
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Closes the database connection
     */
    public void closeDB() {
        dataSource.close();
    }
}
