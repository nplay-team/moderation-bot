package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.modals.ModalEvent;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction
public class ModerationCommands {

    private static final Logger log = LoggerFactory.getLogger(ModerationCommands.class);

    @Inject
    private EmbedCache embedCache;

    private ModerationActCreateBuilder moderationActBuilder;

    @SlashCommand(value = "moderation warn", desc = "Verwarnt einen Benutzer", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void warnMember(CommandEvent event, @Param("Der Benutzer, der verwarnt werden soll.") Member target) {
        this.moderationActBuilder = ModerationService.warn(target).setIssuer(event.getMember());
        event.replyModal("onModerate");
    }

    @Modal(value = "Begründung angeben")
    public void onModerate(ModalEvent event, @TextInput(label = "Begründung der Moderationshandlung", value = "Begründung") String reason) {
        this.moderationActBuilder.setReason(reason);
        var moderation = ModerationService.getModerationAct(this.moderationActBuilder.create());

        var embed = embedCache.getEmbed("moderationActExecuted")
                .injectValue("id", moderation.id())
                .injectValue("type", moderation.type().humanReadableString)
                .injectValue("member", moderation.userId())
                .injectValue("reason", moderation.reason())
                .injectValue("color", EmbedColors.SUCCESS.hexColor);

        event.reply(embed);
    }

}
