package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Interaction
public class ModerationCommands {

    private static final Logger log = LoggerFactory.getLogger(ModerationCommands.class);
    @Inject
    private EmbedCache embedCache;

    private ModerationActCreateBuilder moderationActBuilder;

    private static final String PARAGRAPH_PARAMETER_DESC = "Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?";

    @SlashCommand(value = "moderation warn", desc = "Verwarnt einen Benutzer", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void warnMember(CommandEvent event, @Param("Der Benutzer, der verwarnt werden soll.") Member target, @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        this.moderationActBuilder = ModerationService.warn(target);
        handleModeration(event, paragraph);
    }

    @SlashCommand(value = "moderation timeout", desc = "Versetzt einen Benutzer in den Timeout", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void timeoutMember(CommandEvent event, @Param("Der Benutzer, den in den Timeout versetzt werden soll.") Member target, @Param("Für wie lange der Timeout andauern soll") Duration until, @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        this.moderationActBuilder = ModerationService
                .timeout(target)
                .setDuration(until.getSeconds() * 1000);
        handleModeration(event, paragraph);
    }

    @SlashCommand(value = "moderation kick", desc = "Kickt einen Benutzer vom Server", isGuildOnly = true, enabledFor = Permission.KICK_MEMBERS)
    public void kickMember(CommandEvent event, @Param("Der Benutzer, der gekickt werden soll.") Member target, @Nullable @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        this.moderationActBuilder = ModerationService.kick(target);
        handleModeration(event, paragraph);
    }

    @SlashCommand(value = "moderation ban", desc = "Bannt einen Benutzer vom Server", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void banMember(CommandEvent event, @Param("Der Benutzer, der gekickt werden soll.") Member target, @Optional @Param("Für wie lange der Ban andauern soll") Duration until, @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        if (until != null) {
            this.moderationActBuilder = ModerationService.tempBan(target).setDuration(until.getSeconds() * 1000);
        } else {
            this.moderationActBuilder = ModerationService.ban(target);
        }
        handleModeration(event, paragraph);
    }

    private void handleModeration(CommandEvent event, @Nullable String paragraphId) {
        this.moderationActBuilder.setIssuer(event.getMember());

        if (paragraphId != null) {
            var paragraph = RuleService.getRuleParagraph(Integer.parseInt(paragraphId));
            paragraph.ifPresent(this.moderationActBuilder::setParagraph);
        }
        
        event.replyModal("onModerate");
    }

    @Modal(value = "Begründung angeben")
    public void onModerate(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        this.moderationActBuilder.setReason(reason);
        var moderation = ModerationService.getModerationAct(this.moderationActBuilder.create());

        List<EmbedDTO.Field> fields = new ArrayList<>();

        fields.add(new EmbedDTO.Field("ID", Long.toString(moderation.id()), true));
        fields.add(new EmbedDTO.Field("Betroffener Nutzer", String.format("<@%s>", moderation.userId()), true));
        fields.add(new EmbedDTO.Field("Begründung", moderation.reason().orElse("Keine Begründung angegeben."), false));

        if (moderation.type().isTemp() && moderation.revokeAt().isPresent()) {
            fields.add(new EmbedDTO.Field("Aktiv bis", String.format("<t:%s:f>", moderation.revokeAt().get().getTime() / 1000), true));
        }

        if (moderation.paragraph().isPresent()) {
            fields.add(new EmbedDTO.Field("Regel", moderation.paragraph().get().toString(), true));
        }

        if (moderation.referenceMessage().isPresent()) {
            fields.add(new EmbedDTO.Field("Referenznachricht", moderation.referenceMessage().get().content().orElse("__Inhalt konnte nicht geladen werden__"), false));
        }

        var embed = embedCache.getEmbed("moderationActExecuted")
                .injectValue("type", moderation.type().humanReadableString)
                .injectValue("color", EmbedColors.SUCCESS.hexColor);

        embed.setFields(fields.toArray(new EmbedDTO.Field[0]));
        embed.setFooter(new EmbedDTO.Footer(event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveName()));

        // TODO: Send Message to Modlog Channel

        event.reply(embed);
    }

    @AutoComplete("moderation")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        var rules = RuleService.getParagraphIdMapping();

        Set<Command.Choice> choices = new HashSet<>();

        for (var entry : rules.entrySet()) {
            choices.add(new Command.Choice(entry.getValue().toString(), Integer.toString(entry.getKey())));
        }

        event.replyChoices(choices);
    }

}
