package de.nplay.moderationbot.serverlog.channel;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.EntitySelectMenu;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.channel.ServerlogChannelService.ServerlogChannel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;

import java.util.stream.Collectors;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ServerlogChannelCommands {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "serverlog show", desc = "Zeigt die Serverlog Kanäle", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void onShow(CommandEvent event) {
        var channels = ServerlogChannelService.getServerlogChannels();

        event.with().keepComponents(false).reply(embedCache.getEmbed("serverlogShow")
                .injectValue("list", channels.stream()
                        .map(it -> "<#%d>".formatted(it.channelId()))
                        .collect(Collectors.joining("\n"))
                ).injectValue("color", EmbedColors.SUCCESS)
        );
    }

    @SlashCommand(value = "serverlog manage", desc = "Bearbeitet die Serverlog Einstellungen", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void onManage(CommandEvent event) {
        var channels = ServerlogChannelService.getServerlogChannels();
        if (channels.isEmpty()) {
            event.with().components("onMenu").reply(embedCache.getEmbed("serverlogManage").injectValue("color", EmbedColors.DEFAULT));
            return;
        }

        var menu = (net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu) event.getSelectMenu("onMenu");
        menu.createCopy().setDefaultValues(channels.stream()
                .map(ServerlogChannel::channelId)
                .map(DefaultValue::channel)
                .toList()
        );
        event.with().builder(builder -> builder.addActionRow(menu.asEnabled()))
                .reply(embedCache.getEmbed("serverlogManage").injectValue("color", EmbedColors.DEFAULT));
    }

    @EntitySelectMenu(value = SelectTarget.CHANNEL, channelTypes = ChannelType.TEXT, placeholder = "Wähle einen oder mehrere Textkanäle aus")
    public void onMenu(ComponentEvent event, Mentions mentions) {
        var channels = ServerlogChannelService.updateServerlogChannels(mentions.getChannels());

        event.with().keepComponents(false).reply(embedCache.getEmbed("serverlogShow")
                .injectValue("list", channels.stream()
                        .map(it -> "<#%d>".formatted(it.channelId()))
                        .collect(Collectors.joining("\n"))
                ).injectValue("color", EmbedColors.SUCCESS)
        );
    }
}
