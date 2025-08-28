package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class ModerationEvents {

    public static BotEvent Created(JDA jda, Guild guild, ModerationService.ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationCreateEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Reverted(JDA jda, Guild guild, ModerationService.ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationRevertEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Deleted(JDA jda, Guild guild, ModerationService.ModerationAct moderationAct, User user) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationDeleteEvent", jda, moderationAct, user)
        );
    }

    public static BotEvent BulkMessageDeletion(JDA jda, Guild guild, Integer amount, User user) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getBulkMessageDeletionEmbed(embedCache, amount, user)
        );
    }

    public static BotEvent SpielersucheAusschluss(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(embedCache, target, issuer, false)
        );
    }

    public static BotEvent SpielersucheAusschlussRevert(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (embedCache) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(embedCache, target, issuer, true)
        );
    }

}
