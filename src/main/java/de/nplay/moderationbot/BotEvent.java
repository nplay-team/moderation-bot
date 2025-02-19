package de.nplay.moderationbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class BotEvent {

    private JDA api;
    private Guild guild;

    public BotEvent(JDA api, Guild guild) {
        this.api = api;
        this.guild = guild;
    }

    public JDA getJDA() {
        return api;
    }

    public Guild getGuild() {
        return guild;
    }

}
