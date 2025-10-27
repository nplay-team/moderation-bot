package de.nplay.moderationbot.moderation.act.model;

import de.chojo.sadu.mapper.wrapper.Row;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.sql.Timestamp;

public final class RevertedModerationAct extends ModerationAct {

    private final UserSnowflake revertedBy;
    private final Timestamp revertedAt;
    private final String revertingReason;

    public RevertedModerationAct(Row row) throws SQLException {
        super(row);
        if (!row.getBoolean("reverted")) {
            throw new IllegalStateException("ModerationAct is not reverted!");
        }
        this.revertedBy = UserSnowflake.fromId(row.getLong("reverted_by"));
        this.revertedAt = row.getTimestamp("reverted_at");
        this.revertingReason = row.getString("revert_reason");
    }

    public UserSnowflake revertedBy() {
        return revertedBy;
    }

    public Timestamp revertedAt() {
        return revertedAt;
    }

    public String revertingReason() {
        return revertingReason;
    }
}
