package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.google.inject.Inject;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissionsService.EntityPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Arrays;
import java.util.List;

@Interaction
@SuppressWarnings("ConstantConditions")
public class PermissionsCommands {

    private static final SelectOption NONE_OPTION = SelectOption.of("Keine Berechtigungen (löscht automatisch alle)", "NONE");
    @Inject
    private EmbedCache embedCache;
    private ISnowflake target;

    @Permissions(BotPermissions.PERMISSION_READ)
    @SlashCommand(value = "permissions list", desc = "Zeigt die Berechtigungen eines Benutzers an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onPermissionsList(CommandEvent event,
                                  @Optional @Param("Der Benutzer, dessen Berechtigungen abgerufen werden sollen.")
                                  Member member) {
        Member target = member == null ? event.getMember() : member;

        event.reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", target.getEffectiveName())
                .injectValue("permissions", BotPermissionsService.getUserPermissions(target).readableList())
                .injectValue("color", EmbedColors.DEFAULT.hexColor)
        );
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @SlashCommand(value = "permissions manage member", desc = "Verwaltet die Berechtigungen eines Benutzers.", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onManageMemberPermissions(CommandEvent event,
                                          @Param("Der Benutzer, dessen Berechtigungen bearbeitet werden sollen.")
                                          Member member) {
        target = member;
        replyMenu(event, BotPermissionsService.getUserPermissions(member), member.getEffectiveName());

    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @SlashCommand(value = "permissions manage role", desc = "Verwaltet die Berechtigungen einer Rolle.", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onManageRolePermissions(CommandEvent event,
                                        @Param("Die Rolle, dessen Berechtigungen bearbeitet werden sollen.")
                                        Role role) {
        target = role;
        replyMenu(event, BotPermissionsService.getRolePermissions(role), role.getName());
    }

    private void replyMenu(ReplyableEvent<?> event, EntityPermissions holder, String target) {
        var selectMenu = ((StringSelectMenu) event.getSelectMenu("onPermissionsSelect")).createCopy();
        selectMenu.getOptions().clear();
        Arrays.stream(BotPermissions.BitFields.values()).forEach(it -> selectMenu.addOption(it.displayName, it.name()));
        selectMenu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT)
                .addOptions(NONE_OPTION)
                .setDefaultValues(BotPermissions.decode(holder.permissions())
                        .stream().map(Enum::name)
                        .toList()
                );

        event.with().builder(it -> it.addActionRow(selectMenu.build()))
                .reply(embedCache.getEmbed("permissionsEdit")
                        .injectValue("target", target)
                        .injectValue("color", EmbedColors.DEFAULT.hexColor)
                );
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @com.github.kaktushose.jda.commands.annotations.interactions.StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @com.github.kaktushose.jda.commands.annotations.interactions.SelectOption(label = "dummy option", value = "dummy value")
    @com.github.kaktushose.jda.commands.annotations.interactions.SelectOption(label = "dummy option 2", value = "dummy value 2")
    public void onPermissionsSelect(ComponentEvent event, List<String> selection) {
        int permissions = selection.contains("NONE") ? 0 : BotPermissions.combine(
                selection.stream().map(it -> BotPermissions.BitFields.valueOf(it).value).toList()
        );

        var targetName = "";
        var list = "";
        switch (target) {
            case Member member -> {
                list = BotPermissionsService.updateUserPermissions(member, permissions).readableList();
                targetName = member.getEffectiveName();
            }
            case Role role -> {
                list = BotPermissionsService.updateRolePermissions(role, permissions).readableList();
                targetName = role.getName();
            }
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }

        event.with().keepComponents(false).reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", targetName)
                .injectValue("permissions", list)
                .injectValue("color", EmbedColors.DEFAULT.hexColor)
        );
    }
}
