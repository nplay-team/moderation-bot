package de.nplay.moderationbot.permissions.commands;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.permissions.BotPermissions.BitFields;
import de.nplay.moderationbot.permissions.BotPermissionsService;
import de.nplay.moderationbot.permissions.BotPermissionsService.EntityPermissions;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Role;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Bundle("permissions")
@Interaction("permissions")
@SuppressWarnings("ConstantConditions")
public class RolePermissionsCommand extends PermissionsCommand {

    private @Nullable Role role;
    private @Nullable Integer newPermissions;

    public RolePermissionsCommand() {
        super("role");
    }

    @Permissions(BotPermissions.PERMISSION_READ)
    @Command("role")
    public void onCommand(CommandEvent event, Role role) {
        this.role = role;
        replyList(event, role);
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "edit.remove", style = ButtonStyle.DANGER)
    public void onRemove(ComponentEvent event) {
        event.reply(Container.of(
                TextDisplay.of("confirm"),
                ActionRow.of(Component.button("onConfirm"), Component.button("onCancel"))
        ).withAccentColor(Replies.ERROR));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.confirm", style = ButtonStyle.DANGER)
    public void onConfirm(ComponentEvent event) {
        replyList(event, BotPermissionsService.updateRolePermissions(role, 0), role.getAsMention());
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.cancel", style = ButtonStyle.SECONDARY)
    public void onCancel(ComponentEvent event) {
        replyList(event, role);
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "edit.modify")
    public void onModify(ComponentEvent event) {
        replyModify(event, BotPermissionsService.getRolePermissions(role), role.getAsMention());
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @StringMenu(value = "permissions-select")
    public void onPermissionsSelect(ComponentEvent event, List<String> selection) {
        newPermissions = BotPermissions.combine(selection.stream().map(it -> BitFields.valueOf(it).value).toList());
        event.reply();
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "edit.save", style = ButtonStyle.SUCCESS)
    public void onSave(ComponentEvent event) {
        EntityPermissions permissions;
        if (newPermissions == null) {
            permissions = BotPermissionsService.getRolePermissions(role);
        } else {
            permissions = BotPermissionsService.updateRolePermissions(role, newPermissions);
        }
        replyList(event, permissions, role.getAsMention());
    }
}
