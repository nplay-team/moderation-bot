package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.backend.DurationMax;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationActBuilder;
import de.nplay.moderationbot.moderation.ModerationActType;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationService.ModerationAct;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.nplay.moderationbot.Helpers.UNKNOWN_USER_HANDLER;

@Interaction
public class ModerationCommands {

    @Inject
    private EmbedCache embedCache;
    private ModerationActBuilder moderationActBuilder;
    private static final String PARAGRAPH_PARAMETER_DESC = "Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?";

    @AutoComplete("moderation")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        if (!event.getName().equals("paragraph"))
            return; // TODO: this is temporary, until jda-commands supports selecting options

        var rules = RuleService.getParagraphIdMapping();
        rules.values().removeIf(it -> !it.shortDisplay().toLowerCase().contains(event.getValue().toLowerCase()));
        event.replyChoices(rules.entrySet().stream()
                .map(it -> new Command.Choice(
                        it.getValue().shortDisplay(),
                        Integer.toString(it.getKey()))
                ).toList()
        );
    }

    @SlashCommand(value = "moderation warn", desc = "Verwarnt einen Benutzer", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void warnMember(CommandEvent event,
                           @Param("Der Benutzer, der verwarnt werden soll.") Member target,
                           @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        this.moderationActBuilder = ModerationActBuilder.warn(target, event.getUser()).paragraph(paragraph);
        event.replyModal("onModerate");
    }

    @SlashCommand(value = "moderation timeout", desc = "Versetzt einen Benutzer in den Timeout", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void timeoutMember(CommandEvent event,
                              @Param("Der Benutzer, den in den Timeout versetzt werden soll.") Member target,
                              @Param("Für wie lange der Timeout andauern soll (max. 28 Tage)") @DurationMax(2419200) Duration until,
                              @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        moderationActBuilder = ModerationActBuilder.timeout(target, event.getUser()).duration(until.getSeconds() * 1000).paragraph(paragraph);
        event.replyModal("onModerate");
    }

    @SlashCommand(value = "moderation kick", desc = "Kickt einen Benutzer vom Server", isGuildOnly = true, enabledFor = Permission.KICK_MEMBERS)
    public void kickMember(CommandEvent event,
                           @Param("Der Benutzer, der gekickt werden soll.") Member target,
                           @Nullable @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        moderationActBuilder = ModerationActBuilder.kick(target, event.getUser()).paragraph(paragraph);
        event.replyModal("onModerate");
    }

    @SlashCommand(value = "moderation ban", desc = "Bannt einen Benutzer vom Server", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void banMember(
            CommandEvent event,
            @Param("Der Benutzer, der gekickt werden soll.") Member target,
            @Optional @Param("Für wie lange der Ban andauern soll") Duration until,
            @Optional @Max(7)
            @Param("Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?") Integer delDays,
            @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph
    ) {
        moderationActBuilder = ModerationActBuilder.ban(target, event.getUser()).deletionDays(delDays).paragraph(paragraph);
        if (until != null) {
            moderationActBuilder.type(ModerationActType.TEMP_BAN).duration(until.getSeconds() * 1000);
        }
        event.replyModal("onModerate");
    }

    @Modal(value = "Begründung angeben")
    public void onModerate(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        var action = moderationActBuilder.reason(reason).build();
        var moderationAct = ModerationService.createModerationAct(action);

        List<EmbedDTO.Field> fields = new ArrayList<>();

        fields.add(new EmbedDTO.Field("ID", Long.toString(moderationAct.id()), true));
        fields.add(new EmbedDTO.Field("Betroffener Nutzer", "<@%s>".formatted(moderationAct.userId()), true));
        fields.add(new EmbedDTO.Field("Begründung", java.util.Optional.ofNullable(moderationAct.reason()).orElse("Keine Begründung angegeben."), false));

        if (moderationAct.type().isTemp() && moderationAct.revokeAt() != null) {
            fields.add(new EmbedDTO.Field("Aktiv bis", "<t:%s:f>".formatted(moderationAct.revokeAt().getTime() / 1000), true));
        }

        if (moderationAct.paragraph() != null) {
            fields.add(new EmbedDTO.Field("Regel", moderationAct.paragraph().shortDisplay(), true));
        }

        if (moderationAct.referenceMessage() != null) {
            fields.add(new EmbedDTO.Field("Referenznachricht", moderationAct.referenceMessage().content(), false));
        }

        if (moderationAct.delDays() != null && moderationAct.delDays() > 0) {
            fields.add(new EmbedDTO.Field("Nachrichten löschen", "Für %d Tage".formatted(moderationAct.delDays()), true));
        }

        var embed = embedCache.getEmbed("moderationActExecuted")
                .injectValue("type", moderationAct.type().humanReadableString)
                .injectValue("color", EmbedColors.SUCCESS);

        embed.setFields(fields.toArray(new EmbedDTO.Field[0]));
        embed.setFooter(new EmbedDTO.Footer(event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveName()));

        // Executes the action (e.g. kicks the user)
        action.executor().accept(action);
        
        sendMessageToUser(moderationAct, event);
        event.reply(embed);
    }

    private void sendMessageToUser(ModerationAct moderationAct, ReplyableEvent<?> event) {
        Map<String, Object> defaultInjectValues = Map.of(
                "issuerId", moderationAct.issuerId(),
                "issuerUsername", event.getJDA().retrieveUserById(moderationAct.issuerId()).complete().getName(),
                "reason", java.util.Optional.ofNullable(moderationAct.reason()).orElse("?DEL?"),
                "date", System.currentTimeMillis() / 1000,
                "paragraph", java.util.Optional.ofNullable(moderationAct.paragraph()).map(RuleParagraph::fullDisplay).orElse("?DEL?"),
                "id", moderationAct.id()
        );

        EmbedDTO embedDTO = switch (moderationAct.type()) {
            case WARN -> embedCache.getEmbed("warnEmbed").injectValue("color", EmbedColors.WARNING);
            case TIMEOUT -> embedCache.getEmbed("timeoutEmbed").injectValue("color", EmbedColors.WARNING);
            case KICK -> embedCache.getEmbed("kickEmbed").injectValue("color", EmbedColors.ERROR);
            case TEMP_BAN -> embedCache.getEmbed("tempBanEmbed").injectValue("color", EmbedColors.ERROR);
            case BAN -> embedCache.getEmbed("banEmbed").injectValue("color", EmbedColors.ERROR);
        };

        embedDTO.injectValues(defaultInjectValues);

        if (moderationAct.revokeAt() != null) {
            embedDTO.injectValue("until", moderationAct.revokeAt().getTime() / 1000);
        }

        EmbedBuilder embedBuilder = embedDTO.toEmbedBuilder();
        embedBuilder.getFields().removeIf(it -> "?DEL?".equals(it.getValue()));
        event.getJDA().retrieveUserById(moderationAct.userId())
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build()))
                .queue(_ -> {}, UNKNOWN_USER_HANDLER);
    }
}
