package de.nplay.moderationbot.slowmode;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

public class SlowmodeService {

    public Optional<Slowmode> get(GuildChannel channel) {
        return Query.query("SELECT * FROM slowmode_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .mapAs(Slowmode.class)
                .first();
    }

    public void set(GuildChannel channel, Duration duration) {
        Query.query("INSERT INTO slowmode_channels (channel_id, duration) VALUES (?, ?) ON CONFLICT (channel_id) DO UPDATE SET duration = ?")
                .single(Call.of()
                        .bind(channel.getIdLong())
                        .bind(duration.toSeconds())
                        .bind(duration.toSeconds())
                )
                .insert();
    }

    public void removeSlowmode(GuildChannel channel) {
        Query.query("DELETE FROM slowmode_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .delete();
    }

    public record Slowmode(long channelId, Duration duration) {

        @MappingProvider("")
        public Slowmode(Row row) throws SQLException {
            this(row.getLong("channel_id"), Duration.ofSeconds(row.getInt("duration")));
        }
    }
}
