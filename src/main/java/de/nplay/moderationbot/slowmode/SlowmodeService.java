package de.nplay.moderationbot.slowmode;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.time.Duration;
import java.util.Optional;

public class SlowmodeService {

    public static Optional<Slowmode> getSlowmode(GuildChannel channel) {
        return Query.query("SELECT * FROM slowmode_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .mapAs(Slowmode.class)
                .first();
    }

    public static boolean isSlowmodeEnabled(GuildChannel channel) {
        return Query.query("SELECT EXISTS (SELECT * FROM slowmode_channels WHERE channel_id = ?)")
                .single(Call.of().bind(channel.getIdLong()))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public static void setSlowmode(GuildChannel channel, int duration) {
        Query.query("INSERT INTO slowmode_channels (channel_id, duration) VALUES (?, ?) ON CONFLICT (channel_id) DO UPDATE SET duration = ?")
                .single(Call.of()
                        .bind(channel.getIdLong())
                        .bind(duration)
                        .bind(duration)
                )
                .insert();
    }

    public static void removeSlowmode(GuildChannel channel) {
        Query.query("DELETE FROM slowmode_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .delete();
    }

    public record Slowmode(long channelId, Duration duration) {

        @MappingProvider("")
        public static RowMapping<Slowmode> map() {
            return row -> new Slowmode(
                    row.getLong("channel_id"),
                    Duration.ofSeconds(row.getInt("duration"))
            );
        }
    }
}
