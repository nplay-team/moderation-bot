package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;

public record PermissionsEvent(
        AuditlogType type, UserSnowflake issuer, ISnowflake target, int oldPermissions, int newPermissions
) implements BotEvent { }
