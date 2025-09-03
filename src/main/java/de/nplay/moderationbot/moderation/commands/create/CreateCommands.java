package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.duration.DurationAdapter;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class CreateCommands {

    protected static final DurationAdapter durationAdapter = new DurationAdapter();
    @Inject
    protected ModerationActLock moderationActLock;
    protected ModerationActBuilder moderationActBuilder;
    protected Boolean replyEphemeral = false;
    @Inject
    private Serverlog serverlog;

    public void executeModeration(ReplyableEvent<?> event, String reason) {
        var moderationAct = moderationActBuilder.reason(reason).build().execute(event);

        var embed = event.embed("moderationActExecuted");
        embed.placeholders(entry("type", moderationAct.type().humanReadableString))
                .footer(event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveName())
                .getFields().addAll(generateFields(moderationAct));

        serverlog.onEvent(ModerationEvents.Created(event.getJDA(), event.getGuild(), moderationAct), event);

        event.with().ephemeral(replyEphemeral).embeds(embed).reply();
        moderationActLock.unlock(moderationAct.userId().toString());
    }

    private List<Field> generateFields(ModerationService.ModerationAct moderationAct) {
        List<Field> fields = new ArrayList<>();

        fields.add(new Field("ID", Long.toString(moderationAct.id()), true));
        fields.add(new Field("Betroffener Nutzer", "<@%s>".formatted(moderationAct.userId()), true));
        fields.add(new Field("Begründung", Objects.requireNonNullElse(moderationAct.reason(), "Keine Begründung angegeben."), false));

        if (moderationAct.type().isTemp() && moderationAct.revokeAt() != null) {
            fields.add(new Field("Aktiv bis", "<t:%s:f>".formatted(moderationAct.revokeAt().getTime() / 1000), true));
        }

        if (moderationAct.paragraph() != null) {
            fields.add(new Field("Regel", moderationAct.paragraph().shortDisplay(), true));
        }

        if (moderationAct.referenceMessage() != null) {
            fields.add(new Field("Referenznachricht", moderationAct.referenceMessage().content(), false));
        }

        if (moderationAct.delDays() != null && moderationAct.delDays() > 0) {
            fields.add(new Field("Nachrichten löschen", "Für %d Tage".formatted(moderationAct.delDays()), true));
        }

        return fields;
    }
}
