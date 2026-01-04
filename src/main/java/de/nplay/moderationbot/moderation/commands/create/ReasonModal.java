package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.lock.ModerationActLock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import static de.nplay.moderationbot.moderation.commands.create.CreateCommand.REASON_ID;
import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
public class ReasonModal {

    private final ModerationActLock moderationActLock;
    private final Serverlog serverlog;

    @Inject
    public ReasonModal(ModerationActLock moderationActLock, Serverlog serverlog) {
        this.moderationActLock = moderationActLock;
        this.serverlog = serverlog;
    }

    @Modal(value = "Begründung angeben ($type)")
    public void onModerate(ModalEvent event) {
        ModerationActBuilder builder = event.kv().get("builder", ModerationActBuilder.class).orElseThrow();
        var moderationAct = builder.reason(event.value(REASON_ID).getAsString()).execute(event);

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
        event.with().embeds(embed).reply();
        moderationActLock.unlock(moderationAct.user().getIdLong());
    }
}
