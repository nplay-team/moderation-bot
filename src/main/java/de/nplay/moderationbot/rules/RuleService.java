package de.nplay.moderationbot.rules;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import java.util.*;
import java.util.stream.Collectors;

public class RuleService {

    private static final Set<RuleParagraph> cache = new HashSet<>();

    public static List<RuleParagraph> getRuleParagraphs() {
        return Query.query("SELECT * FROM rule_paragraphs")
                .single()
                .mapAs(RuleParagraph.class)
                .all();
    }

    public static Optional<RuleParagraph> getRuleParagraph(int id) {
        var cacheEntry = cache.stream().filter(it -> it.id == id).findFirst();

        if (cacheEntry.isPresent()) {
            return cacheEntry;
        } else {
            var paragraph = Query.query("SELECT * FROM rule_paragraphs WHERE id = ?")
                    .single(Call.of().bind(id))
                    .mapAs(RuleParagraph.class)
                    .first();

            paragraph.ifPresent(cache::add);
            return paragraph;
        }
    }

    public static Optional<RuleParagraph> getRuleParagraphByDisplayName(String displayName) {
        return getRuleParagraphs()
                .stream()
                .filter(r -> r.shortDisplay().equalsIgnoreCase(displayName))
                .findFirst();
    }

    public static Map<Integer, RuleParagraph> getParagraphIdMapping() {
        return getRuleParagraphs()
                .stream()
                .collect(Collectors.toMap(r -> r.id, r -> r));
    }

    public record RuleParagraph(int id, String number, String title, Optional<String> content) {

        @MappingProvider("")
        public static RowMapping<RuleParagraph> map() {
            return row -> new RuleParagraph(
                    row.getInt("id"),
                    row.getString("number"),
                    row.getString("title"),
                    Optional.ofNullable(row.getString("content"))
            );
        }

        public String shortDisplay() {
            return number + " - " + title;
        }

        public String fullDisplay() {
            return "%s\n%s".formatted(shortDisplay(), content.orElse("/"));
        }
    }
}
