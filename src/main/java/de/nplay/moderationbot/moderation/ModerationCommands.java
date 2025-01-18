package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.interactions.Optional;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.backend.DurationMax;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService.ModerationAct;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;

@Interaction
public class ModerationCommands {

    @Inject
    private EmbedCache embedCache;
    private ModerationActBuilder moderationActBuilder;
    private static final String PARAGRAPH_PARAMETER_DESC = "Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?";

    @AutoComplete("moderation")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        if (!event.getName().equals("paragraphId"))
            return; // TODO: this is temporary, until jda-commands supports selecting options

        var rules = RuleService.getParagraphIdMapping();

        Set<Command.Choice> choices = new HashSet<>();

        for (var entry : rules.entrySet()) {
            if (entry.getValue().toString().toLowerCase().contains(event.getValue().toLowerCase())) {
                choices.add(new Command.Choice(entry.getValue().toString(), Integer.toString(entry.getKey())));
            }
        }

        event.replyChoices(choices);
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
                              @Param("Für wie lange der Timeout andauern soll (max. 28 Tage)") @DurationMax(2419200)
                              Duration until, @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
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
            @Param("Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?")
            Integer delDays,
            @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph
    ) {
        if (until != null) {
            moderationActBuilder = ModerationActBuilder.tempBan(target, event.getUser(), delDays).duration(until.getSeconds() * 1000);
        } else {
            moderationActBuilder = ModerationActBuilder.ban(target, event.getUser(), delDays);
        }
        moderationActBuilder.paragraph(paragraph);
        event.replyModal("onModerate");
    }

    @Modal(value = "Begründung angeben")
    public void onModerate(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        var action = moderationActBuilder.reason(reason).build();
        var moderationAct = ModerationService.createModerationAct(action);

        List<EmbedDTO.Field> fields = new ArrayList<>();

        fields.add(new EmbedDTO.Field("ID", Long.toString(moderationAct.id()), true));
        fields.add(new EmbedDTO.Field("Betroffener Nutzer", String.format("<@%s>", moderationAct.userId()), true));
        fields.add(new EmbedDTO.Field("Begründung", moderationAct.reason().orElse("Keine Begründung angegeben."), false));

        if (moderationAct.type().isTemp() && moderationAct.revokeAt().isPresent()) {
            fields.add(new EmbedDTO.Field("Aktiv bis", String.format("<t:%s:f>", moderationAct.revokeAt().get().getTime() / 1000), true));
        }

        if (moderationAct.paragraph().isPresent()) {
            fields.add(new EmbedDTO.Field("Regel", moderationAct.paragraph().get().toString(), true));
        }

        if (moderationAct.referenceMessage().isPresent()) {
            fields.add(new EmbedDTO.Field("Referenznachricht", moderationAct.referenceMessage().get().content().orElse("__Inhalt konnte nicht geladen werden__"), false));
        }

        if (moderationAct.delDays().isPresent() && moderationAct.delDays().get() > 0) {
            fields.add(new EmbedDTO.Field("Nachrichten löschen", String.format("Für %d Tage", moderationAct.delDays().get()), true));
        }

        var embed = embedCache.getEmbed("moderationActExecuted")
                .injectValue("type", moderationAct.type().humanReadableString)
                .injectValue("color", EmbedColors.SUCCESS);

        embed.setFields(fields.toArray(new EmbedDTO.Field[0]));
        embed.setFooter(new EmbedDTO.Footer(event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveName()));

        action.consumer().accept(action);
        sendMessageToUser(moderationAct, event);
        event.reply(embed);
    }

    private void sendMessageToUser(ModerationAct moderationAct, ReplyableEvent<?> event) {
        var issuerId = moderationAct.issuerId();
        var issuerUsername = event.getJDA().retrieveUserById(issuerId).complete().getEffectiveName();

        Map<String, Object> defaultInjectValues = new HashMap<>();
        defaultInjectValues.put("issuerId", issuerId);
        defaultInjectValues.put("issuerUsername", issuerUsername);
        defaultInjectValues.put("reason", moderationAct.reason().orElse("?DEL?"));
        defaultInjectValues.put("date", System.currentTimeMillis() / 1000);
        defaultInjectValues.put("paragraphId", moderationAct.paragraph().map(ruleParagraph -> ruleParagraph + "\n" + ruleParagraph.content().orElse("/")).orElse("?DEL?"));
        defaultInjectValues.put("id", moderationAct.id());

        EmbedDTO embedDTO = switch (moderationAct.type()) {
            case WARN ->
                    embedCache.getEmbed("warnEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.WARNING);
            case TIMEOUT ->
                    embedCache.getEmbed("timeoutEmbed").injectValues(defaultInjectValues).injectValue("until", moderationAct.revokeAt().get().getTime() / 1000).injectValue("color", EmbedColors.WARNING);
            case KICK ->
                    embedCache.getEmbed("kickEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.ERROR);
            case TEMP_BAN ->
                    embedCache.getEmbed("tempBanEmbed").injectValues(defaultInjectValues).injectValue("until", moderationAct.revokeAt().get().getTime() / 1000).injectValue("color", EmbedColors.ERROR);
            case BAN ->
                    embedCache.getEmbed("banEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.ERROR);
        };

        event.getJDA().retrieveUserById(moderationAct.userId()).queue(user -> {
            EmbedBuilder embedBuilder = embedDTO.toEmbedBuilder();
            embedBuilder.getFields().removeIf(it -> "?DEL?".equals(it.getValue()));
            user.openPrivateChannel().flatMap(it -> it.sendMessageEmbeds(embedBuilder.build())).complete();
        }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_USER));
    }
}
