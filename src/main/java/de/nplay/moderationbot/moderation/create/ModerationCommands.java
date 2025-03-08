package de.nplay.moderationbot.moderation.create;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.duration.DurationAdapter;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationActType;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationUtils;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class ModerationCommands {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Serverlog serverlog;
    private ModerationActBuilder moderationActBuilder;
    private Boolean replyEphemeral = false;
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
        event.replyModal("onModerateWarn");
    }

    @ContextCommand(value = "Verwarne Mitglied", type = Command.Type.USER, isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void warnMemberContext(CommandEvent event, User target) {
        moderationActBuilder = ModerationActBuilder.warn(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateWarn");
    }

    @ContextCommand(value = "Verwarne Mitglied (💬)", type = Command.Type.MESSAGE, isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void warnMemberMessageContext(CommandEvent event, Message target) {
        moderationActBuilder = ModerationActBuilder.warn(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateWarn");
    }

    @SlashCommand(value = "moderation timeout", desc = "Versetzt einen Benutzer in den Timeout", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void timeoutMember(CommandEvent event,
                              @Param("Der Benutzer, den in den Timeout versetzt werden soll.") Member target,
                              @Param("Für wie lange der Timeout andauern soll (max. 28 Tage)") @DurationMax(2419200) Duration until,
                              @Optional @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        moderationActBuilder = ModerationActBuilder.timeout(target, event.getUser()).duration(until.getSeconds() * 1000).paragraph(paragraph);
        event.replyModal("onModerateTimeout");
    }

    @ContextCommand(value = "Timeoute Mitglied", type = Command.Type.USER, isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void timeoutMemberContext(CommandEvent event, User target) {
        moderationActBuilder = ModerationActBuilder.timeout(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateTimeoutContext");
    }

    @ContextCommand(value = "Timeoute Mitglied (💬)", type = Command.Type.MESSAGE, isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void timeoutMemberMessageContext(CommandEvent event, Message target) {
        moderationActBuilder = ModerationActBuilder.timeout(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateTimeoutContext");
    }

    @SlashCommand(value = "moderation kick", desc = "Kickt einen Benutzer vom Server", isGuildOnly = true, enabledFor = Permission.KICK_MEMBERS)
    public void kickMember(CommandEvent event,
                           @Param("Der Benutzer, der gekickt werden soll.") Member target,
                           @Nullable @Param(PARAGRAPH_PARAMETER_DESC) String paragraph) {
        moderationActBuilder = ModerationActBuilder.kick(target, event.getUser()).paragraph(paragraph);
        event.replyModal("onModerateKick");
    }

    @ContextCommand(value = "Kicke Mitglied", type = Command.Type.USER, isGuildOnly = true, enabledFor = Permission.KICK_MEMBERS)
    public void kickMemberContext(CommandEvent event, User target) {
        moderationActBuilder = ModerationActBuilder.kick(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateKick");
    }

    @ContextCommand(value = "Kicke Mitglied (💬)", type = Command.Type.MESSAGE, isGuildOnly = true, enabledFor = Permission.KICK_MEMBERS)
    public void kickMemberMessageContext(CommandEvent event, Message target) {
        moderationActBuilder = ModerationActBuilder.kick(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateKick");
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
            event.replyModal("onModerateTempBan");
        } else event.replyModal("onModerateBan");
    }

    @ContextCommand(value = "(Temp-)Ban Mitglied", type = Command.Type.USER, isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void banMemberContext(CommandEvent event, User target) {
        moderationActBuilder = ModerationActBuilder.ban(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateTempbanContext");
    }

    @ContextCommand(value = "(Temp-)Ban Mitglied (💬)", type = Command.Type.MESSAGE, isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void banMemberMessageContext(CommandEvent event, Message target) {
        moderationActBuilder = ModerationActBuilder.ban(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateTempbanContext");
    }

    @Modal(value = "Begründung angeben (Warn)")
    public void onModerateWarn(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        onModerate(event, reason);
    }

    @Modal(value = "Begründung angeben (Timeout)")
    public void onModerateTimeout(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        onModerate(event, reason);
    }

    @Modal(value = "Begründung angeben (Kick)")
    public void onModerateKick(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        onModerate(event, reason);
    }

    @Modal(value = "Begründung angeben (Temp-Ban)")
    public void onModerateTempBan(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        onModerate(event, reason);
    }

    @Modal(value = "Begründung angeben (Ban)")
    public void onModerateBan(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        onModerate(event, reason);
    }

    public void onModerate(ModalEvent event, String reason) {
        var action = moderationActBuilder.reason(reason).build();
        var moderationAct = ModerationService.createModerationAct(action);

        List<EmbedDTO.Field> fields = new ArrayList<>();

        fields.add(new EmbedDTO.Field("ID", Long.toString(moderationAct.id()), true));
        fields.add(new EmbedDTO.Field("Betroffener Nutzer", "<@%s>".formatted(moderationAct.userId()), true));
        fields.add(new EmbedDTO.Field("Begründung", Objects.requireNonNullElse(moderationAct.reason(), "Keine Begründung angegeben."), false));

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

        serverlog.onEvent(ModerationEvents.Created(event.getJDA(), event.getGuild(), moderationAct));

        ModerationUtils.sendMessageToTarget(moderationAct, event.getJDA(), event.getGuild(), embedCache);
        event.with().ephemeral(replyEphemeral).reply(embed);
    }

    @Modal(value = "Begründung und Dauer angeben")
    public void onModerateTimeoutContext(ModalEvent event,
                                         @TextInput(value = "Begründung der Moderationshandlung") String reason,
                                         @TextInput(value = "Dauer der Moderationshandlung", style = TextInputStyle.SHORT)
                                         String until) {
        var duration = DurationAdapter.parse(until);

        if (duration.isEmpty()) {
            event.with().ephemeral(true).reply("Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an.");
            return;
        }

        if (duration.get().getSeconds() > 2419200) {
            event.with().ephemeral(true).reply("Die angegebene Dauer ist zu lang. Bitte gib eine Dauer von maximal 28 Tagen an.");
            return;
        }

        moderationActBuilder.duration(duration.get().getSeconds() * 1000);
        onModerate(event, reason);
    }

    @Modal(value = "Begründung und Dauer angeben")
    public void onModerateTempbanContext(ModalEvent event,
                                         @TextInput(value = "Begründung der Moderationshandlung") String reason,
                                         @TextInput(value = "Dauer der Moderationshandlung", style = TextInputStyle.SHORT, required = false)
                                             String until) {
        var duration = DurationAdapter.parse(until);
        duration.ifPresent(value -> moderationActBuilder.type(ModerationActType.TEMP_BAN).duration(value.getSeconds() * 1000));
        onModerate(event, reason);
    }

}
