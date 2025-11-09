package de.nplay.moderationbot.serverlog;

import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.embeds.Embed;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.Function;

public record BotEvent(JDA api, Guild guild, Function<ReplyableEvent<?>, Embed> supplier) {

    public JDA jda() {
        return api;
    }

    public Function<ReplyableEvent<?>, Embed> embedSupplier() {
        return supplier;
    }
}
