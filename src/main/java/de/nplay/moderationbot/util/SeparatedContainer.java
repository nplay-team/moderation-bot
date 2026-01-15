package de.nplay.moderationbot.util;

import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.introspection.Introspection;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import io.github.kaktushose.jdac.message.resolver.ComponentResolver;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/// This is a proof of concept and will probably get moved to JDA-Commands
public class SeparatedContainer extends AbstractComponentImpl implements Container, MessageTopLevelComponentUnion {

    private final ComponentResolver<Container> resolver;
    private final List<Entry> placeholders;
    private final @Nullable Separator separator;
    private Container container;
    private @Nullable Footer footer;

    public SeparatedContainer(ContainerChildComponent header, @Nullable Separator separator, Entry... entries) {
        this(Introspection.scopedGet(Property.MESSAGE_RESOLVER), header, separator, entries);
    }

    public SeparatedContainer(Resolver<String> resolver, ContainerChildComponent header, @Nullable Separator separator, Entry... entries) {
        this.separator = separator;
        this.placeholders = new ArrayList<>();
        container = Container.of(header);
        placeholders.addAll(Arrays.asList(entries));
        this.resolver = new ComponentResolver<>(resolver, Container.class);
    }

    public SeparatedContainer add(ContainerChildComponent section, Entry... entries) {
        ArrayList<ContainerChildComponentUnion> components = new ArrayList<>(container.getComponents());
        if (separator != null) {
            components.add((ContainerChildComponentUnion) separator);
        }
        components.add((ContainerChildComponentUnion) section);
        placeholders.addAll(Arrays.asList(entries));
        container = container.withComponents(components);
        return this;
    }

    public SeparatedContainer footer(ContainerChildComponent footer, Entry... entries) {
        return footer(footer, false, entries);
    }

    public SeparatedContainer footer(ContainerChildComponent footer, boolean separate, Entry... entries) {
        this.footer = new Footer(footer, separate);
        placeholders.addAll(Arrays.asList(entries));
        return this;
    }

    @Override
    public SeparatedContainer replace(ComponentReplacer replacer) {
        container = container.replace(replacer);
        return this;
    }

    @Override
    public Type getType() {
        return container.getType();
    }

    @Override
    public int getUniqueId() {
        return container.getUniqueId();
    }

    @Override
    public ActionRow asActionRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Section asSection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TextDisplay asTextDisplay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MediaGallery asMediaGallery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Separator asSeparator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileDisplay asFileDisplay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Container asContainer() {
        return container;
    }

    @Override
    public SeparatedContainer withUniqueId(int uniqueId) {
        container = container.withUniqueId(uniqueId);
        return this;
    }

    @Override
    public SeparatedContainer withAccentColor(@Nullable Integer accentColor) {
        container = container.withAccentColor(accentColor);
        return this;
    }

    @Override
    public SeparatedContainer withSpoiler(boolean spoiler) {
        container = container.withSpoiler(spoiler);
        return this;
    }

    @Override
    public SeparatedContainer withComponents(Collection<? extends ContainerChildComponent> components) {
        container = container.withComponents(components);
        return this;
    }

    @Override
    public @Unmodifiable List<ContainerChildComponentUnion> getComponents() {
        applyFooter();
        container = resolver.resolve(container, Locale.GERMAN, toMap());
        return container.getComponents();
    }

    @Override
    public DataObject toData() {
        applyFooter();
        container = resolver.resolve(container, Locale.GERMAN, toMap());
        return ((ContainerImpl) container).toData();
    }

    @Override
    public @Nullable Integer getAccentColorRaw() {
        return container.getAccentColorRaw();
    }

    @Override
    public boolean isSpoiler() {
        return container.isSpoiler();
    }

    @Override
    public SeparatedContainer withAccentColor(@Nullable Color accentColor) {
        container = container.withAccentColor(accentColor);
        return this;
    }

    @Override
    public SeparatedContainer withComponents(ContainerChildComponent component, ContainerChildComponent... components) {
        container = container.withComponents(component, components);
        return this;
    }

    @Override
    public SeparatedContainer withDisabled(boolean disabled) {
        container = container.withDisabled(disabled);
        return this;
    }

    @Override
    public SeparatedContainer asDisabled() {
        container = container.asDisabled();
        return this;
    }

    @Override
    public SeparatedContainer asEnabled() {
        container = container.asEnabled();
        return this;
    }

    private Map<String, Object> toMap() {
        return placeholders.stream().collect(Collectors.toMap(Entry::name, Entry::value));
    }

    private void applyFooter() {
        if (footer == null) {
            return;
        }

        footer.apply().ifPresent(applied -> {
            footer = applied;

            ArrayList<ContainerChildComponentUnion> components = new ArrayList<>(container.getComponents());
            if (footer.separate() && separator != null) {
                components.addLast((ContainerChildComponentUnion) separator);
            }
            components.addLast((ContainerChildComponentUnion) footer.component());

            container = container.withComponents(components);
        });
    }

    private record Footer(ContainerChildComponent component, boolean separate, boolean applied) {

        public Footer(ContainerChildComponent component, boolean separate) {
            this(component, separate, false);
        }

        public Optional<Footer> apply() {
            if (applied) {
                return Optional.empty();
            }
            return Optional.of(new Footer(component, separate, true));
        }
    }
}
