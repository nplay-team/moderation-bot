package de.nplay.moderationbot.others;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SpecialEventListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        event.getGuild().modifyNickname(event.getMember(), "Erich").queue();
    }

}
