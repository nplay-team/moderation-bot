package de.nplay.moderationbot.moderation.commands.create;

import de.nplay.moderationbot.moderation.act.ModerationActLock;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.UserSnowflake;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public abstract class CreateCommand {

    static final String REASON_ID = "create-reason";
    static final String BUILDER = "builder";
    private final ModerationActLock moderationActLock;

    public CreateCommand(ModerationActLock moderationActLock) {
        this.moderationActLock = moderationActLock;
    }

    public boolean checkLocked(CommandEvent event, UserSnowflake target, UserSnowflake moderator) {
        return moderationActLock.checkLocked(event, target, moderator);
    }

    public void replyModal(CommandEvent event, String type) {
        event.replyModal(
                ReasonModal.class,
                "onModerate",
                Label.of("reason-field", TextInput.of(REASON_ID, TextInputStyle.PARAGRAPH)),
                entry("type", type)
        );
    }
}
