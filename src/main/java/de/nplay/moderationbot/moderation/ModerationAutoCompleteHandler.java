package de.nplay.moderationbot.moderation;

import com.google.inject.Inject;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.function.Function;
import java.util.stream.Collectors;

@Interaction
public class ModerationAutoCompleteHandler {

    private final RuleService ruleService;

    @Inject
    public ModerationAutoCompleteHandler(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @AutoComplete(value = {"mod", "spielersuche ausschluss"}, options = "paragraph")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        var rules = ruleService.getAll().stream().collect(Collectors.toMap(RuleParagraph::id, Function.identity()));

        rules.values().removeIf(it -> !it.shortDisplay().toLowerCase().contains(event.getValue().toLowerCase()));

        event.replyChoices(rules.entrySet().stream()
                .map(it -> new Command.Choice(
                        it.getValue().shortDisplay(),
                        it.getKey())
                ).toList()
        );
    }
}
