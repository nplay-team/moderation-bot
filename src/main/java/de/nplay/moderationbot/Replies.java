package de.nplay.moderationbot;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.awt.*;
import java.sql.Timestamp;

public final class Replies {

    public static final Color STANDARD = Color.decode("#020C24");
    public static final Color WARNING = Color.decode("#FFFF00");
    public static final Color ERROR = Color.decode("#FF0000");
    public static final Color SUCCESS = Color.decode("#00FF00");

    public static Container standard(String key) {
        return of(key, STANDARD);
    }

    public static Container warning(String key) {
        return of(key, WARNING);
    }

    public static Container error(String key) {
        return of(key, ERROR);
    }

    public static Container success(String key) {
        return of(key, SUCCESS);
    }

    private static Container of(String key, Color color) {
        return Container.of(TextDisplay.of(key)).withAccentColor(color);
    }

    public record AbsoluteTime(Timestamp timestamp) {

        public long time() {
            return timestamp.getTime();
        }
    }

    public record RelativeTime(Timestamp timestamp) {

        public long time() {
            return timestamp.getTime();
        }
    }
}
