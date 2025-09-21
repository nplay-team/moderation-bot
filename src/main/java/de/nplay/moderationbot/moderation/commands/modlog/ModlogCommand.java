package de.nplay.moderationbot.moderation.commands.modlog;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    private int offset = 0;
    private int limit = 5;
    private int page = 1;
    private int maxPage = 1;
    private User user;
    @Nullable
    private Member member;

    @Command(value = "mod log")
    public void modlog(CommandEvent event,
                       User target,
                       @Param(optional = true) @Min(1) @Nullable Integer page,
                       @Param(optional = true) @Min(1) @Max(25) @Nullable Integer count) {
        user = target;
        try {
            this.member = event.getGuild().retrieveMember(target).complete();
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() != ErrorResponse.UNKNOWN_MEMBER) {
                throw new IllegalStateException(e);
            }
        }

        if (page != null && count != null) {
            offset = (page - 1) * count;
            this.page = page;
            limit = count;
        }

        maxPage = (int) Math.ceil(ModerationActService.count(target) / (double) limit);
        if (maxPage == 0) {
            maxPage = 1;
        }

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
        event.jdaEvent().deferEdit().complete();
        reply(event);
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
        event.jdaEvent().deferEdit().complete();
        reply(event);
    }

    private void reply(ReplyableEvent<?> event) {
        if (maxPage < 2) {
            event.with().embeds(getEmbeds(event)).reply();
            return;
        }
        var pages = new ArrayList<SelectOption>();
        for (int i = 2; i <= maxPage && i < 26; i++) {
            pages.add(SelectOption.of("Seite " + i, Integer.toString(i)));
        }
        event.with()
                .keepComponents(false)
                .embeds(getEmbeds(event))
                .components(Component.stringSelect("selectPage").selectOptions(pages))
                .components(Component.button("back").enabled(page > 1), Component.button("next").enabled(page < maxPage))
                .reply();
    }

    private Embed[] getEmbeds(ReplyableEvent<?> event) {
        List<Embed> list = new ArrayList<>();

        list.add(header(event, user, member));
        list.add(modlog(event, event.getJDA(), ModerationActService.get(user, limit, offset), page, maxPage));

        var notes = NotesService.getNotesFromUser(user.getIdLong());
        if (!notes.isEmpty()) {
            list.add(1, Helpers.notesEmbed(event, event.getJDA(), user, notes));
        }

        return list.toArray(new Embed[0]);
    }

    private Embed header(ReplyableEvent<?> event, User user, @Nullable Member member) {
        var embed = event.embed("modlogHeader").placeholders(
                entry("name", Helpers.formatUser(event.getJDA(), user)),
                entry("username", user.getEffectiveName()),
                entry("userId", user.getId()),
                entry("avatarUrl", user.getEffectiveAvatarUrl()),
                entry("createdAt", Helpers.formatTimestamp(Timestamp.from(user.getTimeCreated().toInstant())))
        );

        if (member == null) {
            embed.fields().removeByName("Rollen").removeByName("Beigetreten am");
        } else {
            var spielersucheRoleId = ConfigService.get(ConfigService.BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE).orElse("-1");
            if (member.getUnsortedRoles().stream().map(Role::getId).anyMatch(spielersucheRoleId::equals)) {
                embed.placeholders(entry("roles", "<@&%s>".formatted(spielersucheRoleId)));
            } else {
                embed.fields().removeByName("Rollen");
            }
            embed.placeholders(entry("joinedAt", Helpers.formatTimestamp(Timestamp.from(member.getTimeJoined().toInstant()))));

        }
        return embed;
    }

    private Embed modlog(ReplyableEvent<?> event, JDA jda, List<ModerationAct> moderationActs, Integer page, Integer maxPage) {
        var embed = event.embed("modlogActs").placeholders(
                entry("page", page),
                entry("maxPage", maxPage));
        moderationActs.stream().map(it -> it.toField(jda)).forEach(embed.fields()::add);
        return embed;
    }
}
