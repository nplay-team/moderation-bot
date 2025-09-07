package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ModerationEvents {

    public static BotEvent Created(JDA jda, Guild guild, ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getGenericModerationEventEmbed(event, "moderationCreateEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Reverted(JDA jda, Guild guild, ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getGenericModerationEventEmbed(event, "moderationRevertEvent", jda, moderationAct, null)
        );
    }

    public static BotEvent Deleted(JDA jda, Guild guild, ModerationAct moderationAct, User user) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getGenericModerationEventEmbed(event, "moderationDeleteEvent", jda, moderationAct, user)
        );
    }

    public static BotEvent BulkMessageDeletion(JDA jda, Guild guild, Integer amount, User user) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getBulkMessageDeletionEmbed(event, amount, user)
        );
    }

    public static BotEvent SpielersucheAusschluss(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(event, target, issuer, false)
        );
    }

    public static BotEvent SpielersucheAusschlussRevert(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (event) ->
                EmbedHelpers.getSpielersucheAusschlussEmbed(event, target, issuer, true)
        );
    }

}
