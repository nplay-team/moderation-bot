package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.Implementation;
import com.github.kaktushose.jda.commands.dispatching.interactions.Context;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@Implementation
public class CustomPermissionProvider implements PermissionsProvider {

    @Override
    public boolean hasPermission(@NotNull User user, @NotNull Context context) {
        var userPermissions = BotPermissionsService.getUserPermissions(user).permissions();
        if (BotPermissions.hasPermission(userPermissions, BotPermissionBitfield.ADMINISTRATOR)) {
            return true;
        }
        return BotPermissions.hasPermission(BotPermissionsService.getUserPermissions(user).permissions(), context.getInteractionDefinition().getPermissions());
    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull Context context) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        var memberPermissions = BotPermissionsService.getMemberPermissions(member).permissions();
        if (BotPermissions.hasPermission(memberPermissions, BotPermissionBitfield.ADMINISTRATOR)) {
            return true;
        }
        return BotPermissions.hasPermission(memberPermissions, context.getInteractionDefinition().getPermissions());
    }
}
