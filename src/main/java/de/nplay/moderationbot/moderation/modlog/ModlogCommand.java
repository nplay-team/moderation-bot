package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Collection;
import java.util.List;

@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    @Inject
    EmbedCache embedCache;

    private Integer offset = 1;
    private Integer limit = 10;
    private Integer page = 1;

    private Integer maxPage = 1;

    private Member member;
    private InteractionHook interactionHook;

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

        maxPage = (int) Math.ceil(ModerationService.getModerationActCount(member) / (double) limit);

        if (maxPage == 0) maxPage = 1;

        if (this.page > maxPage) {
            this.page = maxPage;
            offset = (this.page - 1) * limit;
        }

        interactionHook = event.jdaEvent()
                .replyEmbeds(getEmbeds(event))
                .addComponents(maxPage > 1 ? getComponents(event) : List.of())
                .complete();
    }

    @Button(value = "Zurück", emoji = "⬅️", style = ButtonStyle.PRIMARY)
    public void back(ComponentEvent event) {
        page--;
        offset -= limit;
        updateMessage(event);
        event.jdaEvent().deferEdit().complete();
    }

    @com.github.kaktushose.jda.commands.annotations.interactions.StringSelectMenu(value = "Seitenauswahl")
    @SelectOption(value = "1", label = "Seite 1")
    @SelectOption(value = "dummy", label = "DO NOT TOUCH") // TODO: Remove when jda-commands updates
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
        interactionHook.editOriginalEmbeds(getEmbeds(event)).queue();
        interactionHook.editOriginalComponents(getComponents(event)).queue();
    }

    public Collection<MessageEmbed> getEmbeds(ReplyableEvent<?> event) {
        return List.of(
                EmbedHelpers.getModlogEmbedHeader(embedCache, member),
                EmbedHelpers.getModlogEmbed(embedCache, event.getJDA(), ModerationService.getModerationActs(member, limit, offset), page, maxPage).toMessageEmbed()
        );
    }

    public Collection<LayoutComponent> getComponents(ReplyableEvent<?> event) {
        var backButton = event.getButton("back");
        var pageSelect = ((StringSelectMenu) event.getSelectMenu("selectPage")).createCopy();
        var nextButton = event.getButton("next");

        var backEnable = page > 1;
        var nextEnable = page < maxPage;

        pageSelect.getOptions().clear();
        pageSelect.addOption("Seite 1", "1");

        if (maxPage > 1) {
            for (int i = 2; i <= maxPage && i < 26; i++) {
                pageSelect.addOption("Seite " + i, Integer.toString(i));
            }
        }

        pageSelect.setMaxValues(1).setDefaultValues(page.toString());

        return List.of(
                ActionRow.of(pageSelect.build()),
                ActionRow.of(backButton.withDisabled(!backEnable), nextButton.withDisabled(!nextEnable))
        );
    }

}
