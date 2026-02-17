package de.nplay.moderationbot.util;

import io.github.kaktushose.jdac.dispatching.reply.dynamic.ButtonComponent;
import io.github.kaktushose.jdac.dispatching.reply.dynamic.menu.StringSelectComponent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/// This is a proof of concept and will probably get moved to JDA-Commands
public class Pagination<T> {

    private final List<Entry> placeholders;
    private final int maxPage;
    private final BiFunction<Integer, Integer, List<ContainerChildComponent>> bodySupplier;
    private final BiFunction<Integer, Integer, TextDisplay> pageCountSupplier;
    private final ButtonComponent previous;
    private final ButtonComponent next;
    private final int limit;
    private Separator separator;
    private @Nullable StringSelectComponent pageJump;
    private Setting pageJumpControl;
    private int pageJumpThreshold;
    private Setting pageCountControl;
    private int pageCountThreshold;
    private int page;
    private int offset;
    private TextDisplay header;
    private SeparatorSetting separatorSetting;

    public Pagination(
            int maxPage,
            BiFunction<Integer, Integer, List<ContainerChildComponent>> bodySupplier,
            BiFunction<Integer, Integer, TextDisplay> pageCountSupplier,
            ButtonComponent next,
            ButtonComponent previous,
            int limit,
            @Nullable StringSelectComponent pageJump,
            TextDisplay header
    ) {
        this.maxPage = maxPage;
        this.bodySupplier = bodySupplier;
        this.pageCountSupplier = pageCountSupplier;
        this.next = next;
        this.previous = previous;
        this.limit = limit;
        this.pageJump = pageJump;
        this.header = header;
        offset = 0;
        page = 1;
        placeholders = new ArrayList<>();
        separator = Separator.createDivider(Separator.Spacing.SMALL);
        pageJumpControl = pageJump == null ? Setting.NEVER : Setting.ALWAYS;
        pageJumpThreshold = 4;
        pageCountControl = Setting.ADAPTIVE;
        pageCountThreshold = 2;
        separatorSetting = SeparatorSetting.BODY;
    }

    public Container current() {
        SeparatedContainer container = new SeparatedContainer(header, separator, placeholders.toArray(Entry[]::new));

        if (separatorSetting == SeparatorSetting.ALWAYS ||separatorSetting == SeparatorSetting.BODY) {
            container.add(separator);
        }
        bodySupplier.apply(page, offset).forEach(it -> add(container, it));
        if (separatorSetting == SeparatorSetting.BODY) {
            container.add(separator);
        }

        switch (pageJumpControl) {
            case ALWAYS -> add(container, ActionRow.of(Objects.requireNonNull(pageJump)));
            case ADAPTIVE -> {
                if (maxPage > pageJumpThreshold) {
                    add(container, ActionRow.of(Objects.requireNonNull(pageJump)));
                }
            }
        }
        add(container, ActionRow.of(previous.enabled(page > 1), next.enabled(page < maxPage)));
        switch (pageCountControl) {
            case ALWAYS -> add(container, pageCountSupplier.apply(page, maxPage));
            case ADAPTIVE -> {
                if (maxPage > pageCountThreshold) {
                    add(container, pageCountSupplier.apply(page, maxPage));
                }
            }
        }

        return container;
    }

    private void add(SeparatedContainer container, ContainerChildComponent component) {
        switch (separatorSetting) {
            case ALWAYS -> container.append(component);
            case BODY, NEVER -> container.add(component);
        }
    }

    public Container previous() {
        page--;
        offset -= limit;
        return current();
    }

    public Container next() {
        page++;
        offset += limit;
        return current();
    }

    public Pagination<T> header(TextDisplay header) {
        this.header = header;
        return this;
    }

    public Pagination<T> entries(Entry... placeholders) {
        this.placeholders.addAll(Arrays.asList(placeholders));
        return this;
    }

    public Pagination<T> separator(SeparatorSetting separatorSetting) {
        this.separatorSetting = separatorSetting;
        return this;
    }

    public Pagination<T> separator(Separator separator) {
        this.separator = separator;
        return this;
    }

    public Pagination<T> separator(SeparatorSetting separatorSetting, Separator separator) {
        this.separatorSetting = separatorSetting;
        return this;
    }

    public Pagination<T> pageJumpControl(Setting pageJumpControl, @Nullable StringSelectComponent pageJump) {
        if (pageJump == null && pageJumpControl != Setting.NEVER) {
            throw new IllegalArgumentException("Cannot enable page jump without component");
        }
        this.pageJumpControl = pageJumpControl;
        this.pageJump = pageJump;
        return this;
    }

    public Pagination<T> pageCountControl(Setting pageCountControl) {
        this.pageCountControl = pageCountControl;
        return this;
    }

    public void pageJumpThreshold(int pageJumpThreshold) {
        this.pageJumpThreshold = pageJumpThreshold;
    }

    public void pageCountThreshold(int pageCountThreshold) {
        this.pageCountThreshold = pageCountThreshold;
    }

    public enum SeparatorSetting {
        ALWAYS,
        NEVER,
        BODY
    }

    public enum Setting {
        ALWAYS,
        NEVER,
        ADAPTIVE
    }
}
