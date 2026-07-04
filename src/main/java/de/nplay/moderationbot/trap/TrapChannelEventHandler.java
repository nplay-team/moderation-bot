package de.nplay.moderationbot.trap;

import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Objects;

public class TrapChannelEventHandler extends ListenerAdapter {

    private final MessageResolver messageResolver;
    private final TrapChannelService service;
    private final ModerationActService moderationActService;

    public TrapChannelEventHandler(MessageResolver messageResolver, TrapChannelService service, ModerationActService moderationActService) {
        this.messageResolver = messageResolver;
        this.service = service;
        this.moderationActService = moderationActService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel channel) {
            if (service.get(channel).isEmpty()) return;
            handleBot(Objects.requireNonNull(event.getMember()), event.getMessage());
        }
    }

    private void handleBot(Member member, Message message) {
        message.delete().queue();

        ModerationActBuilder
                .kick(member, member.getJDA().getSelfUser())
                .reason("Verdacht auf Spam-Bot")
                .deletionDays(1)
                .execute(moderationActService, member.getGuild().getLocale(), member.getJDA(), messageResolver);
    }

}
