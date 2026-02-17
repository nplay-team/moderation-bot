package de.nplay.moderationbot.moderation.act.model;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.introspection.Introspection;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.utils.Checks;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

/// Builder class for creating instances of [ModerationAct] used for creating a new moderation action.
public class ModerationActBuilder {

    private final long issuerId;
    private final long targetId;
    private final Consumer<ModerationActCreateData> executor;
    private ModerationActType type;
    private @Nullable String reason;
    private @Nullable RuleParagraph paragraph;
    private @Nullable Message messageReference;
    private @Nullable Duration duration;
    private int deletionDays;

    private ModerationActBuilder(
            long issuerId,
            ModerationActType type,
            long targetId,
            Consumer<ModerationActCreateData> executor
    ) {
        this.issuerId = issuerId;
        this.type = type;
        this.targetId = targetId;
        this.executor = executor;
    }

    public static ModerationActBuilder warn(UserSnowflake target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.WARN,
                target.getIdLong(),
                _ -> {}
        );
    }

    public static ModerationActBuilder timeout(Member target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.TIMEOUT,
                target.getIdLong(),
                data -> {
                    if (data.revokeAt().isEmpty()) {
                        throw new IllegalStateException("Cannot perform timeout without duration being set!");
                    }
                    target.timeoutUntil(data.revokeAt().get().toInstant()).reason(data.reason()).queue();
                }
        );
    }

    public static ModerationActBuilder kick(Member target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.KICK,
                target.getIdLong(),
                data -> {
                    if (data.deletionDays > 0) {
                        target.ban(data.deletionDays(), TimeUnit.DAYS).queue(_ -> {
                            target.getGuild().unban(target.getUser()).queue();
                        });
                    } else {
                        target.kick().reason(data.reason()).queue();
                    }
                }
        );
    }

    public static ModerationActBuilder ban(Member target, UserSnowflake issuer) {
        return ban(target.getUser(), target.getGuild(), issuer);
    }

    public static ModerationActBuilder ban(UserSnowflake target, Guild guild, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.BAN,
                target.getIdLong(),
                data -> {
                    guild.ban(target, data.deletionDays(), TimeUnit.DAYS).reason(data.reason()).queue();
                }
        );
    }

    public ModerationActBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    public ModerationActBuilder paragraph(@Nullable RuleParagraph paragraph) {
        this.paragraph = paragraph;
        return this;
    }

    public ModerationActBuilder messageReference(@Nullable Message message) {
        this.messageReference = message;
        return this;
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [ModerationActType#TIMEOUT]
    /// [ModerationActType#BAN] or [ModerationActType#TEMP_BAN].
    ///
    /// If this builder was of type [ModerationActType#BAN] will automatically transform this into a temp ban.
    public ModerationActBuilder duration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration cannot be 0 or negative!");
        }
        if (type == ModerationActType.BAN) {
            type = ModerationActType.TEMP_BAN;
        }
        if (type == ModerationActType.TIMEOUT || type == ModerationActType.TEMP_BAN) {
            this.duration = duration;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set duration on moderation act with type: " + type);
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [ModerationActType#BAN],
    /// [ModerationActType#TEMP_BAN] or [ModerationActType#KICK]
    public ModerationActBuilder deletionDays(int days) {
        if (type == ModerationActType.BAN || type == ModerationActType.TEMP_BAN || type == ModerationActType.KICK) {
            deletionDays = days;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set deletion days on moderation act with type: " + type);
    }

    public ModerationAct execute(ReplyableEvent<?> event, ModerationActService service) {
        reason = reason == null ? event.resolve("default-reason") : reason;
        var data = new ModerationActCreateData(targetId, type, issuerId, reason, Optional.ofNullable(messageReference),
                                               Optional.ofNullable(paragraph), duration, deletionDays);
        ModerationAct act = service.create(data);
        executor.accept(data);
        sendModerationToTarget(act, event);
        return act;
    }

    @Bundle("create")
    private void sendModerationToTarget(ModerationAct act, ReplyableEvent<?> event) {
        Color color = switch (act.type()) {
            case WARN, TIMEOUT -> Replies.WARNING;
            case KICK, TEMP_BAN, BAN -> Replies.ERROR;
        };
        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("act-info"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("type", type.localized(event.getUserLocale())),
                entry("description", type)
        ).footer(TextDisplay.of("act-info.footer"), true).withAccentColor(color);

        container.append(
                TextDisplay.of("act-info.reason"),
                entry("id", act.id()),
                entry("reason", act.reason()),
                entry("date", act.createdAt())
        );
        act.revokeAt().ifPresent(it ->
                                         container.append(TextDisplay.of("act-info.revoke"), entry("until", it))
        );
        act.paragraph().ifPresent(it ->
                                          container.append(TextDisplay.of("act-info.paragraph"), entry("paragraph", it.fullDisplay()))
        );
        act.messageReference().ifPresent(it ->
                                                 container.append(TextDisplay.of("act-info.reference"), entry("message", it.content()))
        );

        Helpers.sendDM(act.user(), event.getJDA(),container);
    }

    public enum ModerationActType {
        WARN("default$warn"),
        TIMEOUT("default$timeout"),
        KICK("default$kick"),
        TEMP_BAN("default$temp-ban"),
        BAN("default$ban");

        private final String localizationKey;

        ModerationActType(String localizationKey) {
            this.localizationKey = localizationKey;
        }

        public String localizationKey() {
            return localizationKey;
        }

        public String localized(DiscordLocale locale) {
            return Introspection.scopedGet(Property.MESSAGE_RESOLVER).resolve(localizationKey, locale, Map.of());
        }
    }

    public record ModerationActCreateData(
            long targetId,
            ModerationActType type,
            long issuerId,
            String reason,
            Optional<Message> messageReference,
            Optional<RuleParagraph> ruleParagraph,
            @Nullable Duration duration,
            int deletionDays
    ) {

        public ModerationActCreateData {
            Checks.isSnowflake(targetId + "", "targetId");
            Checks.notNull(type, "ModerationActType");
            Checks.isSnowflake(issuerId + "", "issuerId");
        }

        public Optional<Timestamp> revokeAt() {
            return Optional.ofNullable(duration).map(it -> new Timestamp(System.currentTimeMillis() + it.toMillis()));
        }
    }
}
