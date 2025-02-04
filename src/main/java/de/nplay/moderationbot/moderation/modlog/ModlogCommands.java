package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommands {

    private static final Logger log = LoggerFactory.getLogger(ModlogCommands.class);

    @Inject
    EmbedCache embedCache;

    Integer offset = 1;
    Integer limit = 10;
    Integer page = 1;

    Integer actCount = 0;
    Integer maxPage = 1;

    Member member;
    InteractionHook interactionHook;
    
    @SlashCommand(value = "moderation modlog", desc = "Zeigt den Modlog eines Mitglieds an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void modlog(CommandEvent event, @Param("Der Member, dessen Modlog abgerufen werden soll") Member member,
                       @Optional @Param("Die Seite, die angezeigt werden soll") @Min(1) Integer page,
                       @Optional @Param("Wie viele Moderationshandlungen pro Seite angezeigt werden sollen (max. 25)") @Min(1) @Max(25) Integer count) {
        this.member = member;

        if (page != null) {
            offset = (page - 1) * count;
            this.page = page;
        }

        if (count != null) limit = count;

        actCount = ModerationService.getModerationActCount(member);
        maxPage = (int) Math.ceil(actCount / (double) limit);

        interactionHook = event.jdaEvent()
                .replyEmbeds(getEmbeds())
                .addComponents(getComponents(event))
                .complete();
    }

    @Button(value = "Zurück", emoji = "⬅️", style = ButtonStyle.PRIMARY)
    public void back(ComponentEvent event) {
        page--;
        offset -= limit;
        updateMessage(event);
        event.jdaEvent().deferEdit().complete();
    }

    @StringSelectMenu(value = "Seitenauswahl")
    @com.github.kaktushose.jda.commands.annotations.interactions.SelectOption(value = "1", label = "Seite 1")
    public void selectPage(ComponentEvent event, List<String> values) {
        page = Integer.parseInt(values.get(0));
        offset = (page - 1) * limit;
        event.jdaEvent().deferEdit().complete();
        updateMessage(event);
    }

    @Button(value = "Weiter", emoji = "➡️", style = ButtonStyle.PRIMARY)
    public void next(ComponentEvent event) {
        page++;
        offset += limit;
        updateMessage(event);
        event.jdaEvent().deferEdit().complete();
    }

    public void updateMessage(ComponentEvent event) {
        interactionHook.editOriginalEmbeds(getEmbeds()).queue();
        interactionHook.editOriginalComponents(getComponents(event)).queue();
    }

    public Collection<MessageEmbed> getEmbeds() {
        return List.of(
                ModlogService.getModlogEmbedHeader(embedCache, member).toMessageEmbed(),
                ModlogService.getModlogEmbed(embedCache, ModerationService.getModerationActs(member, limit, offset), page, maxPage).toMessageEmbed()
        );
    }

    public Collection<LayoutComponent> getComponents(ReplyableEvent<?> event) {
        var backButton = event.getButton("back");
        var pageSelect = ((net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu) event.getSelectMenu("selectPage")).createCopy();
        var nextButton = event.getButton("next");

        var backEnable = page > 1;
        var nextEnable = page < maxPage;

        pageSelect.getOptions().clear();

        if (maxPage > 1) {
            for (int i = 2; i <= maxPage; i++) {
                pageSelect.addOption("Seite " + i, Integer.toString(i));
            }
        }

        pageSelect.setMaxValues(1)
                .addOption("Seite 1", "1")
                .setDefaultValues(page.toString());

        return List.of(
                ActionRow.of(pageSelect.build()),
                ActionRow.of(backButton.withDisabled(!backEnable), nextButton.withDisabled(!nextEnable))
        );
    }

}
