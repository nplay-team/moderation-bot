package de.nplay.moderationbot.trap;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class TrapChannelService {

    public Optional<TrapChannel> get(TextChannel channel) {
        return Query.query("SELECT * FROM trap_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .mapAs(TrapChannel.class)
                .first();
    }

    public void remove(TextChannel channel) {
        Query.query("DELETE FROM trap_channels WHERE channel_id = ?")
                .single(Call.of().bind(channel.getIdLong()))
                .delete();
    }

    public record TrapChannel(String channelId, Timestamp createdAt) {
        @MappingProvider("")
        public TrapChannel(Row row) throws SQLException {
            this(
                    row.getString("channel_id"),
                    row.getTimestamp("created_at")
            );
        }
    }

}
