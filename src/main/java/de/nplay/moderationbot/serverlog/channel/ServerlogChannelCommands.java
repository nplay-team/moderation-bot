package de.nplay.moderationbot.serverlog.channel;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ServerlogChannelCommands {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "serverlog setup", desc = "Fügt den aktuellen Channel zu den Serverlog Channels hinzu.", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setup(CommandEvent event) {
        var channel = event.getChannel();
        ServerlogChannelService.addServerlogChannel(channel.getIdLong());
        event.reply(embedCache.getEmbed("serverlogChannelAdd").injectValue("color", EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "serverlog status", desc = "Zeigt an, ob der aktuelle Channel ein Serverlog Channel ist.", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void status(CommandEvent event) {
        var channel = event.getChannel();
        var serverLogChannel = ServerlogChannelService.getServerlogChannel(channel.getIdLong());

        if (serverLogChannel.isEmpty()) {
            event.with().ephemeral(true).reply(embedCache.getEmbed("channelIsNotServerlog").injectValue("color", EmbedColors.DEFAULT));
            return;
        }

        event.with().ephemeral(true).reply(embedCache.getEmbed("channelIsServerlog").injectValue("color", EmbedColors.DEFAULT));
    }

    @SlashCommand(value = "serverlog remove", desc = "Entfernt den aktuellen Channel von den Serverlog Channels.", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void remove(CommandEvent event) {
        var channel = event.getChannel();
        var serverLogChannel = ServerlogChannelService.getServerlogChannel(channel.getIdLong());
        if (serverLogChannel.isEmpty()) {
            event.reply(embedCache.getEmbed("serverlogChannelNotFound").injectValue("color", EmbedColors.ERROR));
            return;
        }
        ServerlogChannelService.removeServerlogChannel(channel.getIdLong());
        event.reply(embedCache.getEmbed("serverlogChannelRemove").injectValue("color", EmbedColors.SUCCESS));
    }

}
