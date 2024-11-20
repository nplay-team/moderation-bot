package de.nplay.moderationbot.test;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationType;
import de.nplay.moderationbot.permissions.BotPermissionFlags;
import net.dv8tion.jda.api.entities.Member;


@Interaction
public class TestCommands {

    @SlashCommand(value = "test ping", desc = "Testet die Erreichbarkeit des Bots.")
    @Permissions(BotPermissionFlags.MODERATION_READ)
    public void onPing(CommandEvent event) {
        event.reply("Test bestanden!");
    }

    @SlashCommand(value = "test moderate", desc = "Testet das Moderationssystem", isGuildOnly = true)
    @Permissions(BotPermissionFlags.MODERATION_CREATE)
    @SuppressWarnings("ConstantConditions")
    public void onModerate(CommandEvent event, Member member) {
        var moderationInsertion = ModerationService.createModeration(member.getIdLong(), ModerationType.WARN, event.getMember().getIdLong(), null, null, null, null);
        var moderation = ModerationService.getModeration(moderationInsertion.keys().getFirst());
        event.reply(moderation.toString());
    }
}
