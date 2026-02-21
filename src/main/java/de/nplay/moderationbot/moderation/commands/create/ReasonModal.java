package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.lock.ModerationActLock;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import static de.nplay.moderationbot.Replies.SUCCESS;
import static de.nplay.moderationbot.moderation.commands.create.CreateCommand.BUILDER;
import static de.nplay.moderationbot.moderation.commands.create.CreateCommand.REASON_ID;
import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
@Bundle("create")
public class ReasonModal {

    private final ModerationActLock moderationActLock;
    private final Serverlog serverlog;

    @Inject
    public ReasonModal(ModerationActLock moderationActLock, Serverlog serverlog) {
        this.moderationActLock = moderationActLock;
        this.serverlog = serverlog;
    }

    @Modal("reason-title")
    public void onModerate(ModalEvent event) {
        ModerationAct act = event.kv().get(BUILDER, ModerationActBuilder.class)
                .orElseThrow()
                .reason(event.value(REASON_ID).getAsString())
                .execute(event);

        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("executed"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("type", act.type().localized(event.getUserLocale())),
                entry("id", act.id()),
                entry("target", act.user()),
                entry("reason", act.reason())
        ).withAccentColor(SUCCESS);
        act.revokeAt().ifPresent(it ->
                container.append(TextDisplay.of("executed.until"), entry("until", it))
        );
        act.paragraph().ifPresent(it ->
                container.append(TextDisplay.of("executed.paragraph"), entry("paragraph", it.shortDisplay()))
        );
        act.referenceMessage().ifPresent(it ->
                container.append(TextDisplay.of("executed.reference"), entry("message", it.content()))
        );
        event.reply(container);

        serverlog.onEvent(ModerationEvents.Created(event.getJDA(), event.getGuild(), act), event);
        moderationActLock.unlock(act.user().getIdLong());
    }
}
