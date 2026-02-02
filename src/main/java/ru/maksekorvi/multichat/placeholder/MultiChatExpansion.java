package ru.maksekorvi.multichat.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.MultiChatPlugin;
import ru.maksekorvi.multichat.chat.ChatManager;
import ru.maksekorvi.multichat.guild.GuildManager;

public class MultiChatExpansion extends PlaceholderExpansion {
    private final MultiChatPlugin plugin;
    private final ChatManager chatManager;
    private final GuildManager guildManager;

    public MultiChatExpansion(MultiChatPlugin plugin, ChatManager chatManager, GuildManager guildManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
        this.guildManager = guildManager;
    }

    @Override
    public String getIdentifier() {
        return "multichat";
    }

    @Override
    public String getAuthor() {
        return "MakseKorvi";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        switch (params.toLowerCase()) {
            case "guild":
                return defaultValue(guildManager.getGuildName(player.getUniqueId()));
            case "guild_rank":
                return defaultValue(guildManager.getGuildRankName(player.getUniqueId()));
            case "guild_rank_id":
                return String.valueOf(guildManager.getGuildRankId(player.getUniqueId()));
            case "guild_level":
                return String.valueOf(guildManager.getGuildLevel(player.getUniqueId()));
            case "guild_points":
                return String.valueOf(guildManager.getGuildPoints(player.getUniqueId()));
            case "guild_currency":
                return String.valueOf(guildManager.getGuildCurrency(player.getUniqueId()));
            case "prefix":
                return chatManager.getPrefix(player);
            case "chat_channel":
                return chatManager.getChannel(player).getId();
            default:
                return "";
        }
    }

    private String defaultValue(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
