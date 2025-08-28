package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import com.github.kaktushose.jda.commands.guice.Implementation;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@Implementation
public class BotPermissionsProvider implements PermissionsProvider {

    @Override
    public boolean hasPermission(User user, InvocationContext<?> context) {
        return BotPermissionsService.getUserPermissions(user).hasPermissions(context);
    }

    @Override
    public boolean hasPermission(Member member, InvocationContext<?> context) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        return BotPermissionsService.getUserPermissions(member).hasPermissions(context);
    }
}
