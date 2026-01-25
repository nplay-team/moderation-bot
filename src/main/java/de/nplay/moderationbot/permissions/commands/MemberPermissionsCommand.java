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
import net.dv8tion.jda.api.entities.Member;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Bundle("permissions")
@Interaction("permissions")
@ReplyConfig(keepComponents = false, allowedMentions = {})
@SuppressWarnings("ConstantConditions")
public class MemberPermissionsCommand extends PermissionsCommand {

    private @Nullable Member member;
    private @Nullable Integer newPermissions;

    public MemberPermissionsCommand() {
        super("member");
    }

    @Command("member")
    public void onCommand(CommandEvent event, Member member) {
        this.member = member;
        replyList(event, member);
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
        replyList(event, BotPermissionsService.updateUserPermissions(member, 0), member.getEffectiveName());
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.cancel", style = ButtonStyle.SECONDARY)
    public void onCancel(ComponentEvent event) {
        replyList(event, member);
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "edit.modify")
    public void onModify(ComponentEvent event) {
        replyModify(event, BotPermissionsService.getUserPermissions(member), member.getEffectiveName());
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
            permissions = BotPermissionsService.getUserPermissions(member);
        } else {
            permissions = BotPermissionsService.updateUserPermissions(member, newPermissions);
        }
        replyList(event, permissions, member.getEffectiveName());
    }
}
