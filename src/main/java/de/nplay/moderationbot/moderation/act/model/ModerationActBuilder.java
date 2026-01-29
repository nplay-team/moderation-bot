package de.nplay.moderationbot.moderation.act.model;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.introspection.Introspection;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.utils.Checks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(ModerationActBuilder.class);
    private final long issuerId;
    private final long targetId;
    private final Consumer<ModerationActCreateData> executor;
    private ModerationActType type;
    private @Nullable String reason;
    private @Nullable Integer paragraphId;
    private @Nullable Message messageReference;
    private @Nullable Duration duration;
    private int deletionDays;

    private ModerationActBuilder(long issuerId, ModerationActType type, long targetId, Consumer<ModerationActCreateData> executor) {
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
                _ -> log.info("User {} has been warned by {}", target, issuer)
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
                    log.info("User {} has been timed out by {} until {}", target, issuer, data.duration());
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
                    log.info("User {} has been kicked by {}", target, issuer);
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
                    log.info("User {} has been{} banned by {}", target, data.revokeAt().isPresent() ? " temp" : "", issuer);
                    guild.ban(target, data.deletionDays(), TimeUnit.DAYS).reason(data.reason()).queue();
                }
        );
    }

    public ModerationActBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    public ModerationActBuilder paragraph(@Nullable String paragraphId) {
        if (paragraphId == null) {
            return this;
        }
        try {
            this.paragraphId = Integer.parseInt(paragraphId);
        } catch (NumberFormatException _) {
            this.paragraphId = RuleService.getRuleParagraphByDisplayName(paragraphId)
                    .map(RuleService.RuleParagraph::id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid paragraph ID or name: " + paragraphId));
        }
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

    public ModerationAct execute(ReplyableEvent<?> event) {
        var data = new ModerationActCreateData(targetId, type, issuerId, reason, messageReference, paragraphId, duration, deletionDays);
        ModerationAct act = ModerationActService.create(data);
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
        act.referenceMessage().ifPresent(it ->
                container.append(TextDisplay.of("act-info.reference"), entry("message", it.content()))
        );

        Helpers.sendDM(act.user(), event.getJDA(), channel -> channel.sendMessageComponents(container).useComponentsV2());
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
            @Nullable String reason,
            @Nullable Message messageReference,
            @Nullable Integer paragraphId,
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

        public Optional<Long> messageReferenceId() {
            return Optional.ofNullable(messageReference).map(ISnowflake::getIdLong);
        }
    }
}
