package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.annotations.interactions.AutoComplete;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.interactions.commands.Command;

@Interaction
public class ModerationAutoCompleteHandler {

    @AutoComplete(value = {"mod", "spielersuche ausschluss"}, options = "paragraph")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        var rules = RuleService.getParagraphIdMapping();
        rules.values().removeIf(it -> !it.shortDisplay().toLowerCase().contains(event.getValue().toLowerCase()));
        event.replyChoices(rules.entrySet().stream()
                .map(it -> new Command.Choice(
                        it.getValue().shortDisplay(),
                        Integer.toString(it.getKey()))
                ).toList()
        );
    }

}
