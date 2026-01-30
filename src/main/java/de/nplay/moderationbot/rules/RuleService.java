package de.nplay.moderationbot.rules;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;

import java.util.*;
import java.util.stream.Collectors;

public class RuleService {

    private final Set<RuleParagraph> cache = new HashSet<>();

    public List<RuleParagraph> getAll() {
        return Query.query("SELECT * FROM rule_paragraphs")
                .single()
                .mapAs(RuleParagraph.class)
                .all();
    }

    public Optional<RuleParagraph> get(int id) {
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
