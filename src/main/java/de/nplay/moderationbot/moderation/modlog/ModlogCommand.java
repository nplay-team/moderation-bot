package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.google.inject.Inject;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    @Inject
    private EmbedCache embedCache;

    private Integer offset = 0;
    private Integer limit = 5;
    private Integer page = 1;

    private Integer maxPage = 1;

    private ModlogContext context;
    private InteractionHook interactionHook;

    public record ModlogContext(@NotNull User user, @Nullable Member member) {}

    @Command(value = "moderation modlog", desc = "Zeigt den Modlog eines Mitglieds an")
    public void modlog(CommandEvent event, @Param("Der User, dessen Modlog abgerufen werden soll")
                       User user,
                       @Optional @Param("Die Seite, die angezeigt werden soll") @Min(1) Integer page,
                       @Optional @Param("Wie viele Moderationshandlungen pro Seite angezeigt werden sollen (max. 25)")
                       @Min(1) @Max(25) Integer count) {
        interactionHook = event.jdaEvent().deferReply().complete();
        Member member;
        try {
             member = event.getGuild().retrieveMember(user).complete();
        } catch (ErrorResponseException exception) {
            if (exception.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                member = null;
            } else {
                throw new IllegalStateException(exception);
            }
        }
        this.context = new ModlogContext(user, member);

        if (page != null) {
            offset = (page - 1) * count;
            this.page = page;
        }

        if (count != null) limit = count;

        maxPage = (int) Math.ceil(ModerationService.getModerationActCount(user) / (double) limit);

        if (maxPage == 0) maxPage = 1;

        if (this.page > maxPage) {
            this.page = maxPage;
            offset = (this.page - 1) * limit;
        }

        reply(event);
    }

    @Button(value = "Zurück", emoji = "⬅️", style = ButtonStyle.PRIMARY)
    public void back(ComponentEvent event) {
        page--;
        offset -= limit;
        reply(event);
        event.jdaEvent().deferEdit().complete();
    }

    @StringSelectMenu(value = "Seitenauswahl")
    @MenuOption(value = "1", label = "Seite 1")
    public void selectPage(ComponentEvent event, List<String> values) {
        page = Integer.parseInt(values.get(0));
        offset = (page - 1) * limit;
        event.jdaEvent().deferEdit().complete();
        reply(event);
    }

    @Button(value = "Weiter", emoji = "➡️", style = ButtonStyle.PRIMARY)
    public void next(ComponentEvent event) {
        page++;
        offset += limit;
        reply(event);
        event.jdaEvent().deferEdit().complete();
    }

    public void reply(ReplyableEvent<?> event) {
        var pages = new ArrayList<SelectOption>();
        if (maxPage > 1) {
            for (int i = 2; i <= maxPage && i < 26; i++) {
                pages.add(SelectOption.of("Seite " + i, Integer.toString(i)));
            }
        }
        event.with()
                .components(Component.stringSelect("selectPage").selectOptions(pages))
                .components(Component.button("back").enabled(page > 1), Component.button("next").enabled(page < maxPage))
                .reply(getEmbeds(event));
    }

    public MessageCreateData getEmbeds(ReplyableEvent<?> event) {
        List<MessageEmbed> list = new ArrayList<>();

        list.add(EmbedHelpers.getModlogEmbedHeader(embedCache, context));
        list.add(EmbedHelpers.getModlogEmbed(embedCache, event.getJDA(), ModerationService.getModerationActs(context.user, limit, offset), page, maxPage).toMessageEmbed());

        var notes = NotesService.getNotesFromUser(context.user.getIdLong());

        if (!notes.isEmpty()) {
            list.add(1, EmbedHelpers.getNotesEmbed(embedCache, event.getJDA(), context.user, notes).toMessageEmbed());
        }

        return new MessageCreateBuilder().setEmbeds(list).build();
    }
}
