package ru.maksekorvi.multichat.moderation;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.maksekorvi.multichat.util.MessageService;

import java.net.InetAddress;

public class ModerationListener implements Listener {
    private final ModerationManager moderationManager;
    private final MessageService messages;

    public ModerationListener(ModerationManager moderationManager, MessageService messages) {
        this.moderationManager = moderationManager;
        this.messages = messages;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        InetAddress address = event.getAddress();
        String ip = address != null ? address.getHostAddress() : null;
        BanRecord ban = moderationManager.getActiveBan(event.getUniqueId(), ip);
        if (ban != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, messages.format("errors.moderation-banned", "{reason}", ban.getReason()));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (moderationManager.isFrozen(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        MuteRecord mute = moderationManager.getActiveMute(event.getPlayer().getUniqueId());
        if (mute != null) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "errors.moderation-muted");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        MuteRecord mute = moderationManager.getActiveMute(event.getPlayer().getUniqueId());
        if (mute != null) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "errors.moderation-muted");
        }
    }
}
