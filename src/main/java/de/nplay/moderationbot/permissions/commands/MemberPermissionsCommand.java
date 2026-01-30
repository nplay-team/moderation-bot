package de.nplay.moderationbot.permissions.commands;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.permissions.BotPermissions.BitFields;
import de.nplay.moderationbot.permissions.PermissionsService;
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
@SuppressWarnings("ConstantConditions")
public class MemberPermissionsCommand extends PermissionsCommand {

    private @Nullable Member member;
    private @Nullable Integer newPermissions;

    @Inject
    public MemberPermissionsCommand(PermissionsService permissionsService) {
        super("member", permissionsService);
    }

    @Permissions(BotPermissions.PERMISSION_READ)
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
        permissionsService.updateUser(member, 0, event.getUser());
        replyList(event, permissionsService.getCombined(member), member.getEffectiveName());
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.cancel", style = ButtonStyle.SECONDARY)
    public void onCancel(ComponentEvent event) {
        replyList(event, member);
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "edit.modify")
    public void onModify(ComponentEvent event) {
        replyModify(event, permissionsService.getUser(member), member.getEffectiveName());
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
        if (newPermissions != null) {
            permissionsService.updateUser(member, newPermissions, event.getUser());
        }
        replyList(event, member);
    }
}
