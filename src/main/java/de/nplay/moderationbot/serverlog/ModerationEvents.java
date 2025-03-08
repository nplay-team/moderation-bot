package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class ModerationEvents {

    public static BotEvent Created(@NotNull JDA jda, @NotNull Guild guild, @NotNull ModerationService.ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationCreateEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Reverted(@NotNull JDA jda, @NotNull Guild guild, @NotNull ModerationService.ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationRevertEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Deleted(@NotNull JDA jda, @NotNull Guild guild, @NotNull ModerationService.ModerationAct moderationAct, @NotNull User user) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationDeleteEvent", jda, moderationAct, user)
        );
    }

    public static BotEvent BulkMessageDeletion(@NotNull JDA jda, @NotNull Guild guild, @NotNull Integer amount, @NotNull User user) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getBulkMessageDeletionEmbed(embedCache, amount, user)
        );
    }

    public static BotEvent SpielersucheAusschluss(@NotNull JDA jda, @NotNull Guild guild, @NotNull User target, @NotNull User issuer) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(embedCache, target, issuer, false)
        );
    }

    public static BotEvent SpielersucheAusschlussRevert(@NotNull JDA jda, @NotNull Guild guild, @NotNull User target, @NotNull User issuer) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(embedCache, target, issuer, true)
        );
    }

}
