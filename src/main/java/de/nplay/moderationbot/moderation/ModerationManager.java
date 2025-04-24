package de.nplay.moderationbot.moderation;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ModerationManager {

    private static final Logger log = LoggerFactory.getLogger(ModerationManager.class);
    private final ConcurrentHashMap<String, String> activeModerationUsers = new ConcurrentHashMap<>();

    public boolean block(String targetId, String moderatorId) {
        if (check(targetId)) return false;

        log.info("Blocking user {}", targetId);

        CompletableFuture.delayedExecutor(1L, TimeUnit.MINUTES).execute(() -> {
            if (!check(targetId)) return;
            release(targetId);
        });

        activeModerationUsers.put(targetId, moderatorId);
        return true;
    }

    public void release(String userId) {
        log.info("Releasing user {}", userId);
        activeModerationUsers.remove(userId);
    }

    public boolean check(String userId) {
        return activeModerationUsers.containsKey(userId);
    }

    @Nullable
    public String get(String userId) {
        return activeModerationUsers.get(userId);
    }

}
