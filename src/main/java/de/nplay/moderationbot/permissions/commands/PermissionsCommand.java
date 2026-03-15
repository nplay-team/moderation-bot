package de.nplay.moderationbot.permissions.commands;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.permissions.PermissionsService;
import de.nplay.moderationbot.permissions.PermissionsService.EntityPermissions;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public abstract class PermissionsCommand {

    private final StableValue<Integer> roleCount = StableValue.of();
    private final String type;
    protected final PermissionsService permissionsService;

    protected PermissionsCommand(String type, PermissionsService permissionsService) {
        this.type = type;
        this.permissionsService = permissionsService;
    }

    protected void replyList(ReplyableEvent<?> event, Role role) {
        Integer count = roleCount.orElseSet(() -> event.getGuild().retrieveRoleMemberCounts().complete().get(role));
        replyList(event, permissionsService.getRole(role), role.getAsMention(), count);
    }

    protected void replyList(ReplyableEvent<?> event, Member member) {
        replyList(event, permissionsService.getCombined(member), member.getEffectiveName(), roleCount.orElse(0));
    }

    protected void replyList(ReplyableEvent<?> event, EntityPermissions permissions, String target) {
        replyList(event, permissions, target, roleCount.orElse(0));
    }

    protected void replyList(ReplyableEvent<?> event, EntityPermissions permissions, String target, int count) {
        boolean enabled = permissions.permissions() != 0;

        Container container = Container.of(
                TextDisplay.of(type),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("list"),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Component.enabled("onModify"), Component.button("onRemove").enabled(enabled))
        ).withAccentColor(Replies.STANDARD);

        event.reply(
                container,
                entry("target", target),
                entry("permissions", permissions.readableList(event)),
                entry("count", count)
        );
    }

    protected void replyModify(ReplyableEvent<?> event, EntityPermissions permissions, String target) {
        boolean enabled = permissions.permissions() != 0;

        ActionRow select = ActionRow.of(Component.stringSelect("onPermissionsSelect")
                .selectOptions(Arrays.stream(BotPermissions.BitFields.values()).map(it -> SelectOption.of(it.localizationKey, it.name())).toList())
                .maxValues(BotPermissions.BitFields.values().length)
                .defaultValues(BotPermissions.decode(permissions.permissions()).stream().map(Enum::name).toList()));

        Container container = Container.of(
                TextDisplay.of(type),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("edit"),
                select,
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Component.enabled("onSave"), Component.button("onRemove").enabled(enabled))
        ).withAccentColor(Replies.WARNING);

        event.reply(
                container,
                entry("target", target),
                entry("hint", event.resolve("%s.%s".formatted(type, "hint"))),
                entry("count", roleCount.orElse(0))
        );
    }
}
