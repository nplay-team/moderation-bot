package de.nplay.moderationbot.permissions;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.dispatching.context.InvocationContext;
import io.github.kaktushose.jdac.guice.Implementation;
import io.github.kaktushose.jdac.permissions.PermissionsProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;


@Implementation
public class BotPermissionsProvider implements PermissionsProvider {

    private final PermissionsService permissionsService;

    @Inject
    public BotPermissionsProvider(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public boolean hasPermission(User user, InvocationContext<?> context) {
        return permissionsService.getUser(user).hasPermissions(context);
    }

    @Override
    public boolean hasPermission(Member member, InvocationContext<?> context) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        return permissionsService.getCombined(member).hasPermissions(context);
    }
}
