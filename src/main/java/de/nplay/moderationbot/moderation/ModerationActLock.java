package de.nplay.moderationbot.moderation;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ModerationActLock {

    private static final Logger log = LoggerFactory.getLogger(ModerationActLock.class);
    private static final ConcurrentHashMap<String, String> activeModerationUsers = new ConcurrentHashMap<>();

    public boolean lock(String targetId, String moderatorId) {
        if (activeModerationUsers.putIfAbsent(targetId, moderatorId) != null) return false;

        log.debug("Locking user {}", targetId);

        CompletableFuture.delayedExecutor(1L, TimeUnit.MINUTES).execute(() -> unlock(targetId));

        return true;
    }

    public void unlock(String userId) {
        log.debug("Unlocking user {}", userId);
        activeModerationUsers.remove(userId);
    }

    @Nullable
    public String get(String userId) {
        return activeModerationUsers.get(userId);
    }

}
