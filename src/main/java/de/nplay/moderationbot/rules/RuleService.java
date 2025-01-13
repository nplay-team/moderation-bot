package de.nplay.moderationbot.rules;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for reading rule paragraphs from the database
 */
public class RuleService {

    /**
     * Gets a {@link List<RuleParagraph>} of all rule paragraphs
     *
     * @return a List containing all {@link RuleParagraph}
     */
    public static List<RuleParagraph> getRuleParagraphs() {
        return Query.query("SELECT * FROM rule_paragraphs")
                .single()
                .mapAs(RuleParagraph.class)
                .all();
    }
    
    /**
     * Gets a {@link RuleParagraph} based on the id
     *
     * @param id the internal id
     * @return an Optional holding the {@link RuleParagraph}
     */
    public static Optional<RuleParagraph> getRuleParagraph(int id) {
        return Query.query("SELECT * FROM rule_paragraphs WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(RuleParagraph.class)
                .first();
    }

    /**
     * Gets a mapping of the internal ids and the paragraph number
     *
     * @return a Map containing all internal ids and their corresponding paragraph number
     */
    public static Map<Integer, RuleParagraph> getParagraphIdMapping() {
        return getRuleParagraphs()
                .stream()
                .collect(Collectors.toMap(r -> r.id, r -> r));
    }

    /**
     * Mapping of a rule paragraph
     *
     * @param id      the internal id of the entry
     * @param number  the paragraph number
     * @param title   the title of the paragraph
     * @param content the content of the paragraph
     */
    public record RuleParagraph(int id, @NotNull String number, @NotNull String title, Optional<String> content) {
        /**
         * Mapping method for the {@link de.chojo.sadu.mapper.rowmapper.RowMapper RowMapper}
         *
         * @return a {@link RowMapping} of this record
         */
        @MappingProvider("")
        public static RowMapping<RuleParagraph> map() {
            return row -> new RuleParagraph(
                    row.getInt("id"),
                    row.getString("number"),
                    row.getString("title"),
                    Optional.ofNullable(row.getString("content"))
            );
        }

        @Override
        public String toString() {
            return number + " - " + title;
        }
    }
}
