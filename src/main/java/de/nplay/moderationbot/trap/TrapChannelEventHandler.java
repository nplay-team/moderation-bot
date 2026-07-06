package de.nplay.moderationbot.trap;

import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

@Bundle("trap")
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
            var member = Objects.requireNonNull(event.getMember());
            if (isImmune(member) || service.get(channel).isEmpty()) {
                return;
            }
            handleSpamBot(member, event.getMessage());
        }
    }

    private boolean isImmune(Member member) {
        return member.hasPermission(Permission.MESSAGE_MANAGE);
    }

    private void handleSpamBot(Member member, Message message) {
        message.delete().queue();

        var locale = member.getGuild().getLocale();

        ModerationActBuilder
                .kick(member, member.getJDA().getSelfUser())
                .reason(messageResolver.resolve("kick-reason", locale))
                .deletionDays(1)
                .execute(moderationActService, locale, member.getJDA(), messageResolver);
    }

}
