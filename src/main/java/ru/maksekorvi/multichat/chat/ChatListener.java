package ru.maksekorvi.multichat.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.maksekorvi.multichat.auth.AuthManager;
import ru.maksekorvi.multichat.guild.GuildManager;
import ru.maksekorvi.multichat.util.MessageService;

public class ChatListener implements Listener {
    private final ChatManager chatManager;
    private final AuthManager authManager;
    private final MessageService messages;

    public ChatListener(ChatManager chatManager, AuthManager authManager, MessageService messages) {
        this.chatManager = chatManager;
        this.authManager = authManager;
        this.messages = messages;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String msg = messages.format("info.join", "{prefix}", chatManager.getPrefix(event.getPlayer()), "{player}", event.getPlayer().getName());
        event.setJoinMessage(msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String msg = messages.format("info.quit", "{prefix}", chatManager.getPrefix(event.getPlayer()), "{player}", event.getPlayer().getName());
        event.setQuitMessage(msg);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!chatManager.isChatEnabled()) {
            event.setCancelled(true);
            messages.send(player, "errors.feature-disabled");
            return;
        }
        if (!authManager.isAuthenticated(player)) {
            event.setCancelled(true);
            authManager.sendAuthPrompt(player);
            return;
        }
        String message = event.getMessage();
        ChatChannel channel = chatManager.getChannel(player);
        String prefix = chatManager.getGlobalPrefix();
        if (message.startsWith(prefix)) {
            channel = ChatChannel.GLOBAL;
            message = message.substring(prefix.length()).trim();
        }
        if (chatManager.getGuildManager().isGuildChatEnabled(player.getUniqueId())) {
            channel = ChatChannel.GUILD;
        }
        if (message.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        String formatted = chatManager.formatMessage(player, channel, message);
        switch (channel) {
            case GLOBAL:
                Bukkit.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formatted));
                break;
            case LOCAL:
                double radius = chatManager.getLocalRadius();
                Bukkit.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().equals(player.getWorld()))
                    .filter(p -> p.getLocation().distance(player.getLocation()) <= radius)
                    .forEach(p -> p.sendMessage(formatted));
                break;
            case GUILD:
                if (!chatManager.isGuildChannel(channel)) {
                    player.sendMessage(formatted);
                    break;
                }
                GuildManager guildManager = chatManager.getGuildManager();
                if (!guildManager.hasGuild(player.getUniqueId())) {
                    messages.send(player, "errors.guild-not-found");
                    return;
                }
                if (!guildManager.canSpeakGuildChat(player)) {
                    return;
                }
                guildManager.sendGuildMessage(player.getUniqueId(), formatted);
                break;
            default:
                Bukkit.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formatted));
        }
    }
}
