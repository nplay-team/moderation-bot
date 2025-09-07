package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import de.nplay.moderationbot.duration.DurationAdapter;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.util.Optional;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class CreateCommands {

    protected static final DurationAdapter durationAdapter = new DurationAdapter();
    protected final ModerationActLock moderationActLock;
    private final Serverlog serverlog;
    protected ModerationActBuilder moderationActBuilder;
    protected boolean replyEphemeral = false;

    public CreateCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        this.moderationActLock = moderationActLock;
        this.serverlog = serverlog;
    }

    public void executeModeration(ReplyableEvent<?> event, String reason) {
        var moderationAct = moderationActBuilder.reason(reason).execute(event);

        var embed = event.embed("moderationActExecuted")
                .placeholders(entry("type", moderationAct.type()))
                .footer(event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveName());

        var fields = embed.fields()
                .add("ID", Long.toString(moderationAct.id()))
                .add("Betroffener Nutzer", moderationAct.user().getAsMention())
                .add("Begründung", Optional.ofNullable(moderationAct.reason()).orElse( "Keine Begründung angegeben."));

        moderationAct.revokeAt().ifPresent(it -> fields.add("Aktiv bis", TimeFormat.DATE_TIME_SHORT.format(it.getTime())));
        moderationAct.paragraph().ifPresent(it -> fields.add("Regel", it.shortDisplay()));
        moderationAct.referenceMessage().ifPresent(it -> fields.add("Referenznachricht", it.content()));

        serverlog.onEvent(ModerationEvents.Created(event.getJDA(), event.getGuild(), moderationAct), event);
        event.with().ephemeral(replyEphemeral).embeds(embed).reply();
        moderationActLock.unlock(moderationAct.user().getIdLong());
    }
}
