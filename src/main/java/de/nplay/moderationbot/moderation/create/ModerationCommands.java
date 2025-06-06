package de.nplay.moderationbot.moderation.create;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import com.google.inject.Inject;
import de.nplay.moderationbot.duration.DurationAdapter;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.ModerationActLock;
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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;

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
    @Inject
    private ModerationActLock moderationActLock;
    private ModerationActBuilder moderationActBuilder;
    private Boolean replyEphemeral = false;
    private static final String PARAGRAPH_PARAMETER_DESC = "Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?";
    private static final String MESSAGELINK_PARAMETER_DESC = "Link zu einer Nachricht, die referenziert werden soll.";
    private ModerationActType type;

    @AutoComplete(value = {"mod", "spielersuche ausschluss"}, options = "paragraph")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        var rules = RuleService.getParagraphIdMapping();
        rules.values().removeIf(it -> !it.shortDisplay().toLowerCase().contains(event.getValue().toLowerCase()));
        event.replyChoices(rules.entrySet().stream()
                .map(it -> new Choice(
                        it.getValue().shortDisplay(),
                        Integer.toString(it.getKey()))
                ).toList()
        );
    }

    @Command(value = "mod warn", desc = "Verwarnt einen Benutzer")
    public void warnMember(CommandEvent event,
                           @Param("Der Benutzer, der verwarnt werden soll.") Member target,
                           @Param(value = PARAGRAPH_PARAMETER_DESC, optional = true) String paragraph,
                           @Param(value = MESSAGELINK_PARAMETER_DESC, optional = true) MessageLink messageLink) {
        if (checkLocked(event, target, event.getUser())) return;
        this.moderationActBuilder = ModerationActBuilder.warn(target, event.getUser()).paragraph(paragraph);
        setMessageReference(event, messageLink);
        type = ModerationActType.WARN;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Command(value = "Verwarne Mitglied", type = Type.USER)
    public void warnMemberContext(CommandEvent event, User target) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.warn(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        type = ModerationActType.WARN;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Command(value = "Verwarne Mitglied (💬)", type = Type.MESSAGE)
    public void warnMemberMessageContext(CommandEvent event, Message target) {
        if (checkLocked(event, target.getAuthor(), event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.warn(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        type = ModerationActType.WARN;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Command(value = "mod timeout", desc = "Versetzt einen Benutzer in den Timeout")
    public void timeoutMember(CommandEvent event,
                              @Param("Der Benutzer, den in den Timeout versetzt werden soll.") Member target,
                              @Param("Für wie lange der Timeout andauern soll (max. 28 Tage)") @DurationMax(2419200)
                              Duration until,
                              @Param(value = PARAGRAPH_PARAMETER_DESC, optional = true) String paragraph,
                              @Param(value = MESSAGELINK_PARAMETER_DESC, optional = true) MessageLink messageLink) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.timeout(target, event.getUser()).duration(until.getSeconds() * 1000).paragraph(paragraph);
        setMessageReference(event, messageLink);
        type = ModerationActType.TIMEOUT;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Timeout)"));
    }

    @Command(value = "Timeoute Mitglied", type = Type.USER)
    public void timeoutMemberContext(CommandEvent event, User target) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.timeout(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        type = ModerationActType.TIMEOUT;
        event.replyModal("onModerateDurationRequired", modal -> modal.title("Begründung und Dauer angeben (Timeout)"));
    }

    @Command(value = "Timeoute Mitglied (💬)", type = Type.MESSAGE)
    public void timeoutMemberMessageContext(CommandEvent event, Message target) {
        if (checkLocked(event, target.getAuthor(), event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.timeout(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        type = ModerationActType.TIMEOUT;
        event.replyModal("onModerateDurationRequired", modal -> modal.title("Begründung und Dauer angeben (Timeout)"));
    }

    @CommandConfig(enabledFor = Permission.KICK_MEMBERS)
    @Command(value = "mod kick", desc = "Kickt einen Benutzer vom Server")
    public void kickMember(CommandEvent event,
                           @Param("Der Benutzer, der gekickt werden soll.") Member target,
                           @Param(value = PARAGRAPH_PARAMETER_DESC, optional = true) String paragraph,
                           @Max(7)
                               @Param(value = "Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?", optional = true) int delDays,
                           @Param(value = MESSAGELINK_PARAMETER_DESC, optional = true) MessageLink messageLink) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.kick(target, event.getUser()).paragraph(paragraph).deletionDays(delDays);
        setMessageReference(event, messageLink);
        type = ModerationActType.KICK;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Kick)"));
    }

    @CommandConfig(enabledFor = Permission.KICK_MEMBERS)
    @Command(value = "Kicke Mitglied", type = Type.USER)
    public void kickMemberContext(CommandEvent event, User target) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.kick(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        type = ModerationActType.KICK;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Kick)"));
    }

    @CommandConfig(enabledFor = Permission.KICK_MEMBERS)
    @Command(value = "Kicke Mitglied (💬)", type = Type.MESSAGE)
    public void kickMemberMessageContext(CommandEvent event, Message target) {
        if (checkLocked(event, target.getAuthor(), event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.kick(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        type = ModerationActType.KICK;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Kick)"));
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "mod ban", desc = "Bannt einen Benutzer vom Server")
    public void banMember(
            CommandEvent event,
            @Param("Der Benutzer, der gekickt werden soll.") User target,
            @Param(value = "Für wie lange der Ban andauern soll", optional = true) Duration until,
            @Max(7)
            @Param(value = "Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?", optional = true)
            int delDays,
            @Param(value = PARAGRAPH_PARAMETER_DESC, optional = true) String paragraph,
            @Param(value = MESSAGELINK_PARAMETER_DESC, optional = true) MessageLink messageLink
    ) {
        if (checkLocked(event, target, event.getUser())) return;

        Member member;
        try {
            member = event.getGuild().retrieveMember(target).complete();
            moderationActBuilder = ModerationActBuilder.ban(member, event.getUser()).deletionDays(delDays).paragraph(paragraph);
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                moderationActBuilder = ModerationActBuilder.ban(target, event.getGuild(), event.getUser()).deletionDays(delDays).paragraph(paragraph);
            } else {
                throw new IllegalStateException(e);
            }
        }

        setMessageReference(event, messageLink);

        if (until != null) {
            moderationActBuilder.type(ModerationActType.TEMP_BAN).duration(until.getSeconds() * 1000);
            type = ModerationActType.TEMP_BAN;
            event.replyModal("onModerate", modal -> modal.title("Begründung und Dauer angeben (Temp-Ban)"));
        } else {
            type = ModerationActType.BAN;
            event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Ban)"));
        }
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "(Temp-)Ban Mitglied", type = Type.USER)
    public void banMemberContext(CommandEvent event, User target) {
        if (checkLocked(event, target, event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.ban(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        type = ModerationActType.TEMP_BAN;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Temp-Ban)"));
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "(Temp-)Ban Mitglied (💬)", type = Type.MESSAGE)
    public void banMemberMessageContext(CommandEvent event, Message target) {
        if (checkLocked(event, target.getAuthor(), event.getUser())) return;
        moderationActBuilder = ModerationActBuilder.ban(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        type = ModerationActType.TEMP_BAN;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Temp-Ban)"));
    }

    @Modal(value = "Begründung und Dauer angeben")
    public void onModerateDurationRequired(ModalEvent event,
                                           @TextInput(value = "Begründung der Moderationshandlung") String reason,
                                           @TextInput(value = "Dauer der Moderationshandlung", style = TextInputStyle.SHORT)
                                           String until) {
        onModerateDuration(event, reason, until);
    }

    @Modal(value = "Begründung und Dauer angeben")
    public void onModerateDuration(ModalEvent event,
                                   @TextInput(value = "Begründung der Moderationshandlung") String reason,
                                   @TextInput(value = "Dauer der Moderationshandlung", style = TextInputStyle.SHORT, required = false)
                                   String until) {
        var duration = DurationAdapter.parse(until);

        if (!"".equals(until) && duration.isEmpty()) {
            event.with().ephemeral(true).reply("Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an.");
            return;
        }

        if (type == ModerationActType.TIMEOUT && duration.get().getSeconds() > 2419200) {
            event.with().ephemeral(true).reply("Die angegebene Dauer ist zu lang. Bitte gib eine Dauer von maximal 28 Tagen an.");
            return;
        }

        moderationActBuilder.duration(duration.get().getSeconds() * 1000);
        onModerate(event, reason);
    }

    @Modal(value = "Begründung angeben")
    public void onModerate(ModalEvent event, @TextInput(value = "Begründung der Moderationshandlung") String reason) {
        var action = moderationActBuilder.reason(reason).build();

        if (type == ModerationActType.TIMEOUT && ModerationService.isTimeOuted(action.targetId())) {
            event.reply(embedCache.getEmbed("userAlreadyTimeOuted").injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (action.type().isBan() && ModerationService.isBanned(action.targetId())) {
            event.reply(embedCache.getEmbed("userAlreadyBanned").injectValue("color", EmbedColors.ERROR));
            return;
        }

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
        moderationActLock.unlock(moderationAct.userId().toString());
    }

    private boolean checkLocked(ReplyableEvent<?> event, UserSnowflake target, UserSnowflake moderator) {
        if (!moderationActLock.lock(target.getId(), moderator.getId())) {
            if (moderator.getId().equals(moderationActLock.get(target.getId()))) return false;

            event.with().ephemeral(true).reply(
                    embedCache.getEmbed("moderationTargetBlocked")
                            .injectValue("targetId", target.getId())
                            .injectValue("moderatorId", moderationActLock.get(target.getId()))
                            .injectValue("color", EmbedColors.ERROR)
            );
            return true;
        }

        return false;
    }

    private void setMessageReference(ReplyableEvent<?> event, MessageLink messageLink) {
        if (messageLink == null) return;

        var guildChannel = event.getGuild().getGuildChannelById(messageLink.channelId());
        if (guildChannel == null) return;

        if (!(guildChannel instanceof MessageChannel messageChannel)) return;

        var message = messageChannel.retrieveMessageById(messageLink.messageId()).complete();
        if (message == null) return;

        moderationActBuilder.messageReference(message);
    }
}
