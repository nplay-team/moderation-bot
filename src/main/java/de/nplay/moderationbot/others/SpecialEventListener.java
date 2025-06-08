package de.nplay.moderationbot.others;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SpecialEventListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (!event.getUser().getId().equals("1239225720895438894")) return;
        if ("Erich \uD83C\uDF08".equals(event.getNewNickname())) return;
        event.getGuild().modifyNickname(event.getMember(), "Erich \uD83C\uDF08").queue();
    }

}
