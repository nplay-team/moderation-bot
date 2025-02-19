package de.nplay.moderationbot.serverlog.channel;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class ServerlogChannelService {

    public static List<ServerlogChannel> getServerlogChannels() {
        return Query.query("SELECT * FROM serverlog_channels")
                .single()
                .mapAs(ServerlogChannel.class)
                .all();
    }

    public static void addServerlogChannel(long channelId) {
        Query.query("INSERT INTO serverlog_channels (channel_id, created_at) VALUES (?, ?)")
                .single(Call.of().bind(channelId).bind(new Timestamp(System.currentTimeMillis())))
                .insert();
    }

    public static Optional<ServerlogChannel> getServerlogChannel(long channelId) {
        return Query.query("SELECT * FROM serverlog_channels WHERE channel_id = ?")
                .single(Call.of().bind(channelId))
                .mapAs(ServerlogChannel.class)
                .first();
    }

    public static void removeServerlogChannel(long channelId) {
        Query.query("DELETE FROM serverlog_channels WHERE channel_id = ?")
                .single(Call.of().bind(channelId))
                .delete();
    }

    public record ServerlogChannel(
            long id,
            long channelId,
            List<String> excludeEvents,
            Timestamp createdAt
    ) {

        @MappingProvider("")
        public static RowMapping<ServerlogChannel> map() {
            return row -> new ServerlogChannel(
                    row.getLong("id"),
                    row.getLong("channel_id"),
                    row.getList("exclude_events"),
                    row.getTimestamp("created_at")
            );
        }

    }

}
