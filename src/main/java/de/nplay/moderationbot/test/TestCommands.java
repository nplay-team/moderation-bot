package de.nplay.moderationbot.test;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import de.nplay.moderationbot.permissions.BotPermissionFlags;

@Interaction
public class TestCommands {

    @SlashCommand(value = "test ping", desc = "Testet die Erreichbarkeit des Bots.")
    @Permissions(BotPermissionFlags.MODERATION_READ)
    public void onPing(CommandEvent event) {
        event.reply("Test bestanden!");
    }
}
