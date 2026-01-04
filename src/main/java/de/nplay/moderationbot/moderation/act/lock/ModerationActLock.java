package de.nplay.moderationbot.moderation.act.lock;

import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ModerationActLock {

    private static final Logger log = LoggerFactory.getLogger(ModerationActLock.class);
    private static final ConcurrentHashMap<Long, Long> activeModeratedUsers = new ConcurrentHashMap<>();

    /// Attempts to lock the given target and moderator based on the event. If the lock is already set, returns `true`
    /// and will send an error message. Else returns `false`.
    ///
    /// @param target    the [target][UserSnowflake] of the moderation act
    /// @param moderator the [moderator][UserSnowflake] performing the moderation act
    /// @return `true` if the moderation act is already locked, else returns `false`
    public boolean checkLocked(UserSnowflake target, UserSnowflake moderator) {
        if (lock(target.getIdLong(), moderator.getIdLong())) {
            return false;
        }
        return moderator.getIdLong() != activeModeratedUsers.get(target.getIdLong());
    }

    public void unlock(long userId) {
        log.debug("Unlocking user {}", userId);
        activeModeratedUsers.remove(userId);
    }

    /// @return `true` if the lock was successful, else false
    private boolean lock(long targetId, long moderatorId) {
        if (activeModeratedUsers.putIfAbsent(targetId, moderatorId) != null) {
            return false;
        }

        log.debug("Locking user {}", targetId);

        CompletableFuture.delayedExecutor(1L, TimeUnit.MINUTES).execute(() -> {
            if (activeModeratedUsers.remove(targetId) != null) {
                log.warn("Automatically unlocking user: {}", targetId);
            }
        });

        return true;
    }
}
