package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.List;

@Interaction
public class BotPermissionsCommands {

    private static final net.dv8tion.jda.api.interactions.components.selections.SelectOption NONE_OPTION =
            net.dv8tion.jda.api.interactions.components.selections.SelectOption.of(
                    "Keine Berechtigungen (löscht automatisch alle)",
                    "NONE"
            );
    @Inject
    private EmbedCache embedCache;
    private Member targetMember;
    private Role targetRole;

    @SlashCommand(value = "permissions list", desc = "Zeigt die Berechtigungen eines Benutzers an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissionFlags.PERMISSION_READ)
    @SuppressWarnings("ConstantConditions")
    public void onPermissionsList(CommandEvent event, @Optional Member member) {
        Member target = member == null ? event.getMember() : member;

        event.reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", target.getEffectiveName())
                .injectValue("permissions", BotPermissions.getPermissionListString(BotPermissionsService.getMemberPermissions(target).permissions()))
                .injectValue("color", EmbedColors.DEFAULT.hexColor)
        );
    }

    @SlashCommand(value = "permissions manage member", desc = "Verwaltet die Berechtigungen eines Benutzers.", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissionFlags.PERMISSION_MANAGE)
    public void onManageMemberPermissions(CommandEvent event, Member member) {
        targetMember = member;

        var menu = ((net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu) event.getSelectMenu("onMemberPermissionSelect")).createCopy();
        menu.getOptions().clear();
        menu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT);

        for (BotPermissionBitfield permission : BotPermissionBitfield.values()) {
            menu.addOption(permission.humanReadableName, permission.name());
        }

        menu.addOptions(NONE_OPTION);

        var currentPermissions = BotPermissionsService.getMemberPermissions(member).permissions();
        menu.setDefaultValues(BotPermissions.decodePermissions(currentPermissions).stream().map(Enum::name).toList());

        event.jdaEvent()
                .replyEmbeds(
                        embedCache.getEmbed("permissionsEdit")
                                .injectValue("target", member.getEffectiveName())
                                .injectValue("color", EmbedColors.DEFAULT.hexColor)
                                .toMessageEmbed()
                )
                .addActionRow(menu.build())
                .queue();
    }

    @SlashCommand(value = "permissions manage role", desc = "Verwaltet die Berechtigungen einer Rolle.", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissionFlags.PERMISSION_MANAGE)
    public void onManageRolePermissions(CommandEvent event, Role role) {
        targetRole = role;

        var menu = ((net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu) event.getSelectMenu("onRolePermissionSelect")).createCopy();
        menu.getOptions().clear();
        menu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT);

        for (BotPermissionBitfield permission : BotPermissionBitfield.values()) {
            menu.addOption(permission.humanReadableName, permission.name());
        }

        menu.addOptions(NONE_OPTION);

        var currentPermissions = BotPermissionsService.getRolePermissions(role.getIdLong()).permissions();
        menu.setDefaultValues(BotPermissions.decodePermissions(currentPermissions).stream().map(Enum::name).toList());

        event.jdaEvent()
                .replyEmbeds(
                        embedCache.getEmbed("permissionsEdit")
                                .injectValue("target", role.getName())
                                .injectValue("color", EmbedColors.DEFAULT.hexColor)
                                .toMessageEmbed()
                )
                .addActionRow(menu.build())
                .queue();
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @SelectOption(label = "dummy option", value = "dummy value")
    @Permissions(BotPermissionFlags.PERMISSION_MANAGE)
    public void onMemberPermissionSelect(ComponentEvent event, List<String> selection) {
        int permissions = selection.contains("NONE") ? 0 : BotPermissions.combine(
                selection.stream().map(it -> BotPermissionBitfield.valueOf(it).value).toList()
        );

        BotPermissionsService.setUserPermissions(targetMember, permissions);

        event.with().keepComponents(false).reply(
                embedCache.getEmbed("memberPermissionsList")
                        .injectValue("target", targetMember.getEffectiveName())
                        .injectValue("permissions", BotPermissions.getPermissionListString(BotPermissionsService.getUserPermissions(targetMember).permissions()))
                        .injectValue("color", EmbedColors.DEFAULT.hexColor)
        );
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @SelectOption(label = "dummy option", value = "dummy value")
    @Permissions(BotPermissionFlags.PERMISSION_MANAGE)
    public void onRolePermissionSelect(ComponentEvent event, List<String> selection) {
        int permissions = selection.contains("NONE") ? 0 : BotPermissions.combine(
                selection.stream().map(it -> BotPermissionBitfield.valueOf(it).value).toList()
        );

        BotPermissionsService.setRolePermissions(targetRole.getIdLong(), permissions);

        event.with().keepComponents(false).reply(
                embedCache.getEmbed("rolePermissionsList")
                        .injectValue("target", targetRole.getName())
                        .injectValue("permissions", BotPermissions.getPermissionListString(BotPermissionsService.getRolePermissions(targetRole.getIdLong()).permissions()))
                        .injectValue("color", EmbedColors.DEFAULT.hexColor)
        );
    }
}
