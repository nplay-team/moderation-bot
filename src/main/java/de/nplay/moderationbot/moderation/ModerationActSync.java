package de.nplay.moderationbot.moderation;

import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActType;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class ModerationActSync extends ListenerAdapter {

    private final MessageResolver resolver;
    private final ModerationActService actService;

    public ModerationActSync(MessageResolver resolver, ModerationActService actService) {
        this.resolver = resolver;
        this.actService = actService;
    }

    @Override
    public void onGuildAuditLogEntryCreate(@NonNull GuildAuditLogEntryCreateEvent event) {
        if (event.getEntry().getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }

        AuditLogEntry entry = event.getEntry();
        ModerationActType type = switch (entry.getType()) {
            case KICK -> ModerationActType.KICK;
            case BAN -> ModerationActType.BAN;
            default -> null;
        };
        if (type == null) {
            return;
        }

        ModerationActCreateData data = new ModerationActCreateData(
                entry.getTargetIdLong(),
                type,
                entry.getUserIdLong(),
                resolver.resolve("manual-moderation-sync", event.getGuild().getLocale(), entry("reason", entry.getReason())),
                Optional.empty(),
                Optional.empty(),
                null,
                0
        );

        actService.create(data);
    }
}
