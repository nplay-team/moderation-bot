package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.StringSelectMenu;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import de.nplay.moderationbot.permissions.BotPermissionsService.EntityPermissions;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction("permissions")
@SuppressWarnings("ConstantConditions")
public class PermissionsCommands {

    private static final String NONE_OPTION = "NONE";
    private ISnowflake target;

    @Permissions(BotPermissions.PERMISSION_READ)
    @Command("list")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void onPermissionsList(CommandEvent event, Optional<Member> member) {
        Member target = member.orElse(event.getMember());

        event.with().embeds("permissionsList",
                entry("target", target.getEffectiveName()),
                entry("permissions", BotPermissionsService.getUserPermissions(target).readableList())
        ).reply();
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Command("manage member")
    public void onManageMemberPermissions(CommandEvent event, Member member) {
        target = member;
        replyMenu(event, BotPermissionsService.getUserPermissions(member), member.getEffectiveName());
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Command("manage role")
    public void onManageRolePermissions(CommandEvent event, Role role) {
        target = role;
        replyMenu(event, BotPermissionsService.getRolePermissions(role), role.getName());
    }

    private void replyMenu(ReplyableEvent<?> event, EntityPermissions holder, String target) {
        event.with().components(Component.stringSelect("onPermissionsSelect")
                        .selectOptions(SelectOption.of(
                                event.i18n().localize(event.getUserLocale().toLocale(), "permissions-none"),
                                NONE_OPTION)
                        ).selectOptions(Arrays.stream(BotPermissions.BitFields.values())
                                .map(it -> SelectOption.of(it.displayName, it.name()))
                                .toList()
                        ).maxValues(BotPermissions.BitFields.values().length)
                        .defaultValues(BotPermissions.decode(holder.permissions()).stream().map(Enum::name).toList())
                ).embeds("permissionsEdit", entry("target", target))
                .reply();
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @StringSelectMenu(value = "permissions-select")
    public void onPermissionsSelect(ComponentEvent event, List<String> selection) {
        int permissions = selection.contains(NONE_OPTION) ? 0 : BotPermissions.combine(
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

        event.with().keepComponents(false)
                .embeds("permissionsList", entry("target", targetName), entry("permissions", list))
                .reply();
    }
}
