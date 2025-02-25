package de.nplay.moderationbot.config;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigService {

    public static Optional<String> get(String name) {
        return Query.query("SELECT value FROM configs WHERE name = ?")
                .single(Call.of().bind(name))
                .mapAs(java.lang.String.class)
                .first();
    }

    public static Map<String, String> getAll() {
        Map<String, String> map = new HashMap<>();
        Query.query("SELECT name, value FROM configs")
                .single()
                .mapAs(Config.class)
                .all()
                .forEach(config -> map.put(config.name(), config.value()));
        return map;
    }

    public static void set(String name, java.lang.String value) {
        Query.query("INSERT INTO configs (name, value) VALUES (?, ?) ON CONFLICT (name) DO UPDATE SET value = EXCLUDED.value")
                .single(Call.of()
                        .bind(name)
                        .bind(value)
                )
                .insert();
    }

    public record Config(java.lang.String name, java.lang.String value) {

        @MappingProvider("")
        public static RowMapping<Config> map() {
            return row -> new Config(row.getString("name"), row.getString("value"));
        }

    }

}
