package de.nplay.moderationbot.embeds;

public enum EmbedColors {
    DEFAULT("#020c24"),
    ERROR("#ff0000"),
    SUCCESS("#00ff00"),
    WARNING("#ffff00");
    
    public final String hexColor;
    
    EmbedColors(String hexColor) {
        this.hexColor = hexColor;
    }
}
