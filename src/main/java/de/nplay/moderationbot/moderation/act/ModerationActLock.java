package de.nplay.moderationbot.moderation.act;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class ModerationActLock {

    private static final Logger log = LoggerFactory.getLogger(ModerationActLock.class);
    private static final ConcurrentHashMap<Long, Long> activeModerationUsers = new ConcurrentHashMap<>();

    /// Attempts to lock the given target and moderator based on the event. If the lock is already set, returns `true`
    /// and will send an error message. Else returns `false`.
    ///
    /// @param event the corresponding [ReplyableEvent] of the moderation act
    /// @param target the [target][UserSnowflake] of the moderation act
    /// @param moderator the [moderator][UserSnowflake] performing the moderation act
    ///
    /// @return `true` if the moderation act is already locked, else returns `false`
    public boolean checkLocked(ReplyableEvent<?> event, UserSnowflake target, UserSnowflake moderator) {
        if (lock(target.getIdLong(), moderator.getIdLong())) {
            return false;
        }

        if (moderator.getIdLong() == activeModerationUsers.get(target.getIdLong())) {
            return false;
        }

        event.with().ephemeral(true)
                .embeds("moderationTargetBlocked",
                        entry("target", target.getAsMention()),
                        entry("moderator", UserSnowflake.fromId(activeModerationUsers.get(target.getIdLong())).getAsMention())
                ).reply();

        return true;
    }

    public void unlock(long userId) {
        log.debug("Unlocking user {}", userId);
        activeModerationUsers.remove(userId);
    }

    /// @return `true` if the lock was successful, else false
    private boolean lock(long targetId, long moderatorId) {
        if (activeModerationUsers.putIfAbsent(targetId, moderatorId) != null) {
            return false;
        }

        log.debug("Locking user {}", targetId);

        CompletableFuture.delayedExecutor(1L, TimeUnit.MINUTES).execute(() -> {
            if (activeModerationUsers.remove(targetId) != null) {
                log.warn("Automatically unlocking user: {}", targetId);
            }
        });

        return true;
    }
}
