package de.nplay.moderationbot.moderation.commands.modlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.notes.NotesCommands;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.notes.NotesService.Note;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.embeds.Embed;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    private static final int PAGE_COUNT_ID = 10000;
    private static final int BUTTON_BACK_ID = 10001;
    private static final int BUTTON_FORTH_ID = 10002;
    private int offset = 0;
    private int limit = 5;
    private int page = 1;
    private int maxPage = 1;
    private User user;
    private @Nullable Member member;
    private int maxId;

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

        event.reply(getModlog(event));
    }

    @Button(value = "Zurück", emoji = "⬅️", style = ButtonStyle.PRIMARY, uniqueId = BUTTON_BACK_ID)
    public void back(ComponentEvent event) {
        page--;
        offset -= limit;
        event.jdaEvent().deferEdit().complete();
        replyEdit(event, page + 1);
    }

    @StringSelectMenu(value = "Seitenauswahl")
    @MenuOption(value = "1", label = "Seite 1")
    public void selectPage(ComponentEvent event, List<String> values) {
        int oldPage = page;
        page = Integer.parseInt(values.get(0));
        offset = (page - 1) * limit;
        event.jdaEvent().deferEdit().complete();
        replyEdit(event, oldPage);
    }

    @Button(value = "Weiter", emoji = "➡️", style = ButtonStyle.PRIMARY, uniqueId = BUTTON_FORTH_ID)
    public void next(ComponentEvent event) {
        page++;
        offset += limit;
        event.jdaEvent().deferEdit().complete();
        replyEdit(event, page - 1);
    }

    private MessageTopLevelComponent getModlog(ReplyableEvent<?> event) {
        List<ContainerChildComponent> container = new ArrayList<>(List.of(
                Section.of(
                        Thumbnail.fromUrl(user.getEffectiveAvatarUrl()),
                        TextDisplay.of("## NPLAY-Moderation - Datenauskunft\n### %s".formatted(
                                Helpers.formatUser(event.getJDA(), user)
                        ))),
                Separator.createInvisible(Separator.Spacing.SMALL),
                TextDisplay.of("**Nutzer ID:** %d\n**Erstellt am:** %s\n**Beigetreten am:** %s".formatted(
                        user.getIdLong(),
                        Helpers.formatTimestamp(Timestamp.from(user.getTimeCreated().toInstant())),
                        member == null ? "N/A" : Helpers.formatTimestamp(Timestamp.from(member.getTimeJoined().toInstant())) // TODO: remove member null check with more elegant way
                )),
                Separator.createDivider(Separator.Spacing.LARGE)
        ));

        List<Note> notes = NotesService.getNotesFromUser(user.getIdLong());
        if (!notes.isEmpty()) {
            container.add(TextDisplay.of("## Notizen"));
            for (Note note : notes) {
                container.add(note.toTextDisplay(event.getJDA()));
            }
            container.add(Separator.createDivider(Separator.Spacing.LARGE));
        }

        int uniqueId = page * 100;
        container.add(TextDisplay.of("## Moderationshandlungen"));
        for (ModerationAct moderationAct : ModerationActService.get(user, limit, offset)) {
            container.add(moderationAct.toTextDisplay(event).withUniqueId(uniqueId++));
            maxId = uniqueId;
            System.out.println(uniqueId);
        }

        // only show navigation buttons if there is more than one page
        if (!(maxPage < 2)) {
            var pages = new ArrayList<SelectOption>();
            for (int i = 2; i <= maxPage && i < 26; i++) {
                pages.add(SelectOption.of("Seite " + i, Integer.toString(i)));
            }

            container.add(Separator.createDivider(Separator.Spacing.LARGE));

            container.add(ActionRow.of(
                    Component.stringSelect("selectPage").selectOptions(pages))
            );
            container.add(ActionRow.of(
                    Component.button("back").enabled(page > 1),
                    Component.button("next").enabled(page < maxPage)
            ));
            container.add(Separator.createInvisible(Separator.Spacing.SMALL));
            container.add(TextDisplay.of("-# Seite (%s/%s)".formatted(page, maxPage)).withUniqueId(PAGE_COUNT_ID));
        }

        return Container.of(container);
    }

    private void replyEdit(ComponentEvent event, int oldPage) {
        int index = 0;
        int newIds = page * 100;

        List<ComponentReplacer> replacer = new ArrayList<>();
        List<ModerationAct> moderationActs = ModerationActService.get(user, limit, offset);

        for (int oldId = (oldPage) * 100; oldId < maxId; oldId++) {
            TextDisplay newComponent = null; // remove "overhang", edge case: new page has fewer items than oldPage
            if (index < moderationActs.size()) {
                newComponent = moderationActs.get(index++).toTextDisplay(event).withUniqueId(newIds++);
            }
            replacer.add(ComponentReplacer.byUniqueId(oldId, newComponent));
        }

        replacer.add(ComponentReplacer.byUniqueId(PAGE_COUNT_ID, TextDisplay.of("-# Seite (%s/%s)".formatted(page, maxPage)).withUniqueId(PAGE_COUNT_ID)));
        replacer.add(ComponentReplacer.byUniqueId(BUTTON_BACK_ID, Component.button("back").enabled(page > 1)));
        replacer.add(ComponentReplacer.byUniqueId(BUTTON_FORTH_ID, Component.button("next").enabled(page < maxPage)));
        event.reply(ComponentReplacer.all(replacer));
    }

    private Embed[] getEmbeds(ReplyableEvent<?> event) {
        List<Embed> list = new ArrayList<>();

        list.add(header(event, user, member));
        list.add(modlog(event, ModerationActService.get(user, limit, offset), page, maxPage));

        var notes = NotesService.getNotesFromUser(user.getIdLong());
        if (!notes.isEmpty()) {
            list.add(1, NotesCommands.notesEmbed(event, event.getJDA(), user, notes));
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
            embed.fields().remove("{ $roles }").remove("{ $joinedAt }");
        } else {
            var spielersucheRoleId = ConfigService.get(ConfigService.BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE).orElse("-1");
            if (member.getUnsortedRoles().stream().map(Role::getId).anyMatch(spielersucheRoleId::equals)) {
                embed.placeholders(entry("roles", "<@&%s>".formatted(spielersucheRoleId)));
            } else {
                embed.fields().remove("{ $roles }");
            }
            embed.placeholders(entry("joinedAt", Helpers.formatTimestamp(Timestamp.from(member.getTimeJoined().toInstant()))));

        }
        return embed;
    }

    private Embed modlog(ReplyableEvent<?> event, List<ModerationAct> moderationActs, Integer page, Integer maxPage) {
        var embed = event.embed("modlogActs").placeholders(
                entry("page", page),
                entry("maxPage", maxPage));
        moderationActs.stream().map(it -> it.toField(event)).forEach(embed.fields()::add);
        return embed;
    }
}
