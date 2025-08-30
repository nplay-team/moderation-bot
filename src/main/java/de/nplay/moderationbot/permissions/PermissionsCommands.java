package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.google.inject.Inject;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissionsService.EntityPermissions;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.Arrays;
import java.util.List;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
@SuppressWarnings("ConstantConditions")
public class PermissionsCommands {

    private static final SelectOption NONE_OPTION = SelectOption.of("Keine Berechtigungen (löscht automatisch alle)", "NONE");

    private ISnowflake target;

    @Permissions(BotPermissions.PERMISSION_READ)
    @Command(value = "permissions list", desc = "Zeigt die Berechtigungen eines Benutzers an")
    public void onPermissionsList(CommandEvent event,
                                  @Param(value = "Der Benutzer, dessen Berechtigungen abgerufen werden sollen.", optional = true)
                                  Member member) {
        Member target = member == null ? event.getMember() : member;

        event.reply(event.embed("permissionsList")
                .placeholders(
                        entry("target", target.getEffectiveName()),
                        entry("permissions", BotPermissionsService.getUserPermissions(target).readableList())
                )
                .build()
        );
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Command(value = "permissions manage member", desc = "Verwaltet die Berechtigungen eines Benutzers.")
    public void onManageMemberPermissions(CommandEvent event,
                                          @Param("Der Benutzer, dessen Berechtigungen bearbeitet werden sollen.")
                                          Member member) {
        target = member;
        replyMenu(event, BotPermissionsService.getUserPermissions(member), member.getEffectiveName());

    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Command(value = "permissions manage role", desc = "Verwaltet die Berechtigungen einer Rolle.")
    public void onManageRolePermissions(CommandEvent event,
                                        @Param("Die Rolle, dessen Berechtigungen bearbeitet werden sollen.")
                                        Role role) {
        target = role;
        replyMenu(event, BotPermissionsService.getRolePermissions(role), role.getName());
    }

    private void replyMenu(ReplyableEvent<?> event, EntityPermissions holder, String target) {
        event.with()
                .components(Component.stringSelect("onPermissionsSelect")
                        .selectOptions(NONE_OPTION)
                        .selectOptions(
                                Arrays.stream(BotPermissions.BitFields.values())
                                        .map(it -> SelectOption.of(it.displayName, it.name()))
                                        .toList()
                        )
                        .defaultValues(BotPermissions.decode(holder.permissions()).stream().map(Enum::name).toList())
                )
                .embeds(event.embed("permissionsEdit").placeholders(entry("target", target)))
                .reply();
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
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

        event.with().keepComponents(false).embeds(event.embed("permissionsList")
                .placeholders(entry("target", targetName), entry("permissions", list))
        ).reply();
    }
}
