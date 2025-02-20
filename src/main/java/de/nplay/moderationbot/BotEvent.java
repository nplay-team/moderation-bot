package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.Function;

public class BotEvent {

    private final JDA api;
    private final Guild guild;
    private final Function<EmbedCache, EmbedDTO> supplier;

    public BotEvent(JDA api, Guild guild, Function<EmbedCache, EmbedDTO> supplier) {
        this.api = api;
        this.guild = guild;
        this.supplier = supplier;
    }

    public JDA jda() {
        return api;
    }

    public Guild guild() {
        return guild;
    }

    public Function<EmbedCache, EmbedDTO> embedSupplier() {
        return supplier;
    }
}
