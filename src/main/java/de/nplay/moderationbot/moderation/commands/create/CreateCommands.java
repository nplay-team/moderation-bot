package de.nplay.moderationbot.moderation.commands.create;

import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class CreateCommands {

    protected static final String REASON_ID = "create-reason";
    protected final ModerationActLock moderationActLock;
    private final Serverlog serverlog;
    protected ModerationActBuilder moderationActBuilder;
    protected boolean replyEphemeral = false;

    public CreateCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        this.moderationActLock = moderationActLock;
        this.serverlog = serverlog;
    }

    public void executeModeration(ModalEvent event) {
        var moderationAct = moderationActBuilder.reason(event.value(REASON_ID).getAsString()).execute(event);

        var embed = event.embed("moderationActExecuted")
                .placeholders(entry("type", moderationAct.type()))
                .footer(event.getMember().getEffectiveName(), event.getMember().getEffectiveAvatarUrl());

        var fields = embed.fields()
                .add(event.resolve("id"), Long.toString(moderationAct.id()))
                .add(event.resolve("act-target"), moderationAct.user().getAsMention())
                .add(event.resolve("act-reason"), moderationAct.reason());

        moderationAct.revokeAt().ifPresent(it -> fields.add(event.resolve("active-until"), TimeFormat.DATE_TIME_SHORT.format(it.getTime())));
        moderationAct.paragraph().ifPresent(it -> fields.add(event.resolve("rule"), it.shortDisplay()));
        moderationAct.referenceMessage().ifPresent(it -> fields.add(event.resolve("reference-message"), it.content()));

        serverlog.onEvent(ModerationEvents.Created(event.getJDA(), event.getGuild(), moderationAct), event);
        event.with().ephemeral(replyEphemeral).embeds(embed).reply();
        moderationActLock.unlock(moderationAct.user().getIdLong());
    }
}
