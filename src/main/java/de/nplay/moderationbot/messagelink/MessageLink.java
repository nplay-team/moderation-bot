package de.nplay.moderationbot.messagelink;

import java.util.Optional;

public record MessageLink(String messageId, String channelId, String guildId) {

    /**
     * Creates a {@link MessageLink} from a discord message link.
     *
     * @param link The message link to parse. The link should be in the format: <br />
     *             <code>https://discord.com/channels/{guildId}/{channelId}/{messageId}</code>
     * @return A {@link MessageLink} object containing the messageId, channelId and guildId
     * @throws IllegalArgumentException if the link is not valid
     */
    public static Optional<MessageLink> ofString(String link) {
        if (!validateLink(link)) return Optional.empty();
        String[] parts = link.split("/");
        return Optional.of(new MessageLink(parts[6], parts[5], parts[4]));
    }

    private static boolean validateLink(String link) {
        return link.matches("https://discord.com/channels/\\d+/\\d+/\\d+");
    }

    public String discordLink() {
        return "https://discord.com/channels/%s/%s/%s".formatted(guildId, channelId, messageId);
    }

}
