package de.nplay.moderationbot.permissions;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.permissions.BotPermissions.BitFields;
import de.nplay.moderationbot.permissions.BotPermissionsService.EntityPermissions;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("permissions")
@Interaction("permissions")
@ReplyConfig(keepComponents = false)
@SuppressWarnings("ConstantConditions")
public class MemberPermissionsCommand {

    private @Nullable Member member;
    private @Nullable Integer newPermissions;

    @Command("member")
    public void onCommand(CommandEvent event, Member member) {
        this.member = member;
        replyList(event, BotPermissionsService.getUserPermissions(member));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "member.remove", style = ButtonStyle.DANGER)
    public void onRemove(ComponentEvent event) {
        event.reply(Container.of(
                TextDisplay.of("confirm"),
                ActionRow.of(Component.button("onConfirm"), Component.button("onCancel"))
        ).withAccentColor(Replies.ERROR));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.confirm", style = ButtonStyle.DANGER)
    public void onConfirm(ComponentEvent event) {
        replyList(event, BotPermissionsService.updateUserPermissions(member, 0));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "confirm.cancel", style = ButtonStyle.SECONDARY)
    public void onCancel(ComponentEvent event) {
        replyList(event, BotPermissionsService.getUserPermissions(member));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "member.modify")
    public void onModify(ComponentEvent event) {
        replyModify(event, BotPermissionsService.getUserPermissions(member));
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @StringMenu(value = "permissions-select")
    public void onPermissionsSelect(ComponentEvent event, List<String> selection) {
        newPermissions = BotPermissions.combine(selection.stream().map(it -> BitFields.valueOf(it).value).toList());
        event.reply();
    }

    @Permissions(BotPermissions.PERMISSION_MANAGE)
    @Button(value = "member.save", style = ButtonStyle.SUCCESS)
    public void onSave(ComponentEvent event) {
        EntityPermissions permissions;
        if (newPermissions == null) {
            permissions = BotPermissionsService.getUserPermissions(member);
        } else {
            permissions = BotPermissionsService.updateUserPermissions(member, newPermissions);
        }
        replyList(event, permissions);
    }

    private void replyList(ReplyableEvent<?> event, EntityPermissions permissions) {
        boolean enabled = permissions.permissions() != 0;

        Container container = Container.of(
                TextDisplay.of("member"),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("member.list"),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Component.enabled("onModify"), Component.button("onRemove").enabled(enabled))
        ).withAccentColor(Replies.STANDARD);

        event.reply(container, entry("member", member.getEffectiveName()), entry("permissions", permissions.readableList(event)));
    }

    private void replyModify(ReplyableEvent<?> event, EntityPermissions permissions) {
        boolean enabled = permissions.permissions() != 0;

        ActionRow select = ActionRow.of(Component.stringSelect("onPermissionsSelect")
                .selectOptions(Arrays.stream(BitFields.values()).map(it -> SelectOption.of(it.localizationKey, it.name())).toList())
                .maxValues(BitFields.values().length)
                .defaultValues(BotPermissions.decode(permissions.permissions()).stream().map(Enum::name).toList()));

        Container container = Container.of(
                TextDisplay.of("member"),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("member.edit"),
                select,
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Component.enabled("onSave"), Component.button("onRemove").enabled(enabled))
        ).withAccentColor(Replies.WARNING);

        event.reply(container, entry("member", member.getEffectiveName()));
    }
}
