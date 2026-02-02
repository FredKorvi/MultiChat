package ru.maksekorvi.multichat.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.guild.GuildManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    private final ConfigManager configManager;
    private final MessageService messages;
    private final GuildManager guildManager;
    private final Chat vaultChat;
    private final Map<UUID, ChatChannel> channels = new ConcurrentHashMap<>();
    private final boolean placeholderEnabled;

    public ChatManager(JavaPlugin plugin, ConfigManager configManager, MessageService messages, GuildManager guildManager, Chat vaultChat) {
        this.configManager = configManager;
        this.messages = messages;
        this.guildManager = guildManager;
        this.vaultChat = vaultChat;
        this.placeholderEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public void reload() {
        channels.clear();
    }

    public ChatChannel getChannel(Player player) {
        return channels.computeIfAbsent(player.getUniqueId(), uuid -> {
            String defaultChannel = configManager.getConfig().getString("settings.chat.defaultChannel", "local");
            return ChatChannel.fromId(defaultChannel);
        });
    }

    public void setChannel(Player player, ChatChannel channel) {
        channels.put(player.getUniqueId(), channel);
    }

    public String formatMessage(Player player, ChatChannel channel, String message) {
        String format = configManager.getChat().getString("channels." + channel.getId() + ".format", "{player}: {message}");
        String guild = guildManager.getGuildName(player.getUniqueId());
        String guildTag = guild == null ? "&7(Не в гильдии)&r" : "&7[&a" + guild + "&7]&r";
        String prefix = getPrefix(player);
        String result = format
            .replace("{guild}", guildTag)
            .replace("{guild_rank}", guildManager.getGuildRankName(player.getUniqueId()))
            .replace("{prefix}", prefix)
            .replace("{player}", player.getName())
            .replace("{message}", message);
        result = MessageService.colorize(result);
        if (placeholderEnabled) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    public int getLocalRadius() {
        return configManager.getChat().getInt("channels.local.radius", 35);
    }

    public String getGlobalPrefix() {
        return configManager.getChat().getString("options.prefixGlobal", "!");
    }

    public String getPrefix(Player player) {
        String prefix = getLuckPermsPrefix(player);
        if (prefix != null && !prefix.isEmpty()) {
            return MessageService.colorize(prefix);
        }
        if (vaultChat != null) {
            return MessageService.colorize(vaultChat.getPlayerPrefix(player));
        }
        return "";
    }

    private String getLuckPermsPrefix(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return null;
        }
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getMetaData().getPrefix();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean isGuildChannel(ChatChannel channel) {
        return channel == ChatChannel.GUILD;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public boolean isChatEnabled() {
        return configManager.isFeatureEnabled("chat");
    }
}
