package de.nplay.moderationbot.test;

import com.github.kaktushose.jda.commands.annotations.interactions.ContextCommand;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.Command;


@Interaction
public class TestCommands {

    @SlashCommand(value = "test ping", desc = "Testet die Erreichbarkeit des Bots.")
    @Permissions(BotPermissions.MODERATION_READ)
    public void onPing(CommandEvent event) {
        event.reply("Test bestanden!");
    }

    @SlashCommand(value = "test moderate", desc = "Testet das Moderationssystem", isGuildOnly = true)
    @Permissions(BotPermissions.MODERATION_CREATE)
    @SuppressWarnings("ConstantConditions")
    public void onModerate(CommandEvent event, Member member) {
        long id = ModerationService.warn(member).setIssuer(event.getMember()).create();
        var moderation = ModerationService.getModerationAct(id);
        event.reply(moderation.toString());
    }

    @ContextCommand(value = "test", type = Command.Type.MESSAGE)
    public void onTest(CommandEvent event, Message message) {
        long id = ModerationService.warn(event.getMember()).setIssuer(event.getMember()).setMessageReference(message).create();
        var moderation = ModerationService.getModerationAct(id);
        event.reply(moderation.toString());
    }

}
