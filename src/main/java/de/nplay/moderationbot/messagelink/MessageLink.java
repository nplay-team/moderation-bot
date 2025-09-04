package de.nplay.moderationbot.messagelink;

import java.util.Optional;

public record MessageLink(String messageId, String channelId, String guildId) {

    /// Creates a [MessageLink] from a discord message link.
    ///
    /// @param link The message link to parse. The link should be in the format:
    ///             `https://discord.com/channels/{guildId}/{channelId}/{messageId}`
    /// @return A [MessageLink] object containing the messageId, channelId and guildId
    /// @throws IllegalArgumentException if the link is not valid
    public static Optional<MessageLink> ofString(String link) {
        if (!link.matches("https://discord.com/channels/\\d+/\\d+/\\d+")) {
            return Optional.empty();
        }
        String[] parts = link.split("/");
        return Optional.of(new MessageLink(parts[6], parts[5], parts[4]));
    }

}
