package de.nplay.moderationbot.serverlog.channel;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ServerlogChannelCommands {

    @SlashCommand(value = "serverlog setup", desc = "Fügt den aktuellen Channel zu den Serverlog Channels hinzu.", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setup(CommandEvent event) {
        var channel = event.getChannel();
        ServerlogChannelService.addServerlogChannel(channel.getIdLong());
        event.reply("Serverlog Channel hinzugefügt.");
    }

}
