package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.annotations.Implementation;
import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

@Implementation
public class CustomPermissionProvider implements PermissionsProvider {

    @Override
    public boolean hasPermission(@NotNull User user, @NotNull InvocationContext<?> context) {
        var userPermissions = BotPermissionsService.getUserPermissions(user).permissions();
        if (BotPermissions.hasPermission(userPermissions, BotPermissionBitfield.ADMINISTRATOR)) {
            return true;
        }
        return BotPermissions.hasPermission(BotPermissionsService.getUserPermissions(user).permissions(), new HashSet<>(context.definition().permissions()));
    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull InvocationContext<?> context) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        var memberPermissions = BotPermissionsService.getMemberPermissions(member).permissions();
        if (BotPermissions.hasPermission(memberPermissions, BotPermissionBitfield.ADMINISTRATOR)) {
            return true;
        }
        return BotPermissions.hasPermission(memberPermissions, new HashSet<>(context.definition().permissions()));
    }

}
