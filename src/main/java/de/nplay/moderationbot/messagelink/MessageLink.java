package de.nplay.moderationbot.messagelink;

public class MessageLink {

    private final String messageId;
    private final String channelId;
    private final String guildId;

    public MessageLink(String messageId, String channelId, String guildId) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
    }

    /**
     * Creates a {@link MessageLink} from a discord message link.
     *
     * @param link The message link to parse. The link should be in the format: <br />
     *             <code>https://discord.com/channels/{guildId}/{channelId}/{messageId}</code>
     * @return A {@link MessageLink} object containing the messageId, channelId and guildId
     * @throws IllegalArgumentException if the link is not valid
     */
    public static MessageLink ofString(String link) {
        if (!validateLink(link)) {
            throw new IllegalArgumentException("Invalid message link: " + link);
        }
        String[] parts = link.split("/");
        return new MessageLink(parts[6], parts[5], parts[4]);
    }

    /**
     * Validates a discord message link. The link should be in the format: <br />
     * <code>https://discord.com/channels/{guildId}/{channelId}/{messageId}</code>
     *
     * @param link The message link to validate
     * @return true if the link is valid, false otherwise
     */
    public static boolean validateLink(String link) {
        return link.matches("https://discord.com/channels/\\d+/\\d+/\\d+");
    }

    public String getDiscordLink() {
        return "https://discord.com/channels/%s/%s/%s".formatted(guildId, channelId, messageId);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getGuildId() {
        return guildId;
    }

}
