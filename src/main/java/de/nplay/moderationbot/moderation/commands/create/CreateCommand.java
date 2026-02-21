package de.nplay.moderationbot.moderation.commands.create;

import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("create")
public abstract class CreateCommand {

    static final String REASON_ID = "create-reason";
    static final String BUILDER = "create-builder";

    public void replyModal(CommandEvent event, String type) {
        event.replyModal(
                ReasonModal.class,
                "onModerate",
                Label.of("reason-label", TextInput.of(REASON_ID, TextInputStyle.PARAGRAPH)),
                entry("type", type)
        );
    }
}
