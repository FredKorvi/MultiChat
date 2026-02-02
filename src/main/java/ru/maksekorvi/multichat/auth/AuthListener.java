package ru.maksekorvi.multichat.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.maksekorvi.multichat.util.MessageService;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class AuthListener implements Listener {
    private final AuthManager authManager;
    private final MessageService messages;

    public AuthListener(AuthManager authManager, MessageService messages) {
        this.authManager = authManager;
        this.messages = messages;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        authManager.loadPlayer(event.getPlayer());
        applyAuthEffects(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authManager.unregister(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (shouldBlock(event.getPlayer())) {
            applyAuthEffects(event.getPlayer());
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (shouldBlock(event.getPlayer())) {
            event.setCancelled(true);
            authManager.sendAuthPrompt(event.getPlayer());
            applyAuthEffects(event.getPlayer());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (shouldBlock(player)) {
            List<String> whitelist = authManager.getCommandWhitelist();
            String cmd = event.getMessage().toLowerCase();
            boolean allowed = whitelist.stream().anyMatch(cmd::startsWith);
            if (!allowed) {
                event.setCancelled(true);
                authManager.sendAuthPrompt(player);
                applyAuthEffects(player);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (shouldBlock(event.getPlayer())) {
            applyAuthEffects(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (shouldBlock(event.getPlayer())) {
            applyAuthEffects(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (shouldBlock(event.getPlayer())) {
            applyAuthEffects(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (shouldBlock(event.getPlayer())) {
            applyAuthEffects(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (shouldBlock(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (shouldBlock(player)) {
                applyAuthEffects(player);
                event.setCancelled(true);
            }
        }
    }

    private boolean shouldBlock(Player player) {
        if (!authManager.isAuthEnabled()) {
            return false;
        }
        if (authManager.isLoading(player.getUniqueId())) {
            return true;
        }
        return !authManager.isAuthenticated(player);
    }

    private void applyAuthEffects(Player player) {
        if (!authManager.isAuthEnabled()) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false));
        String title = messages.get("auth.title");
        if (!title.isEmpty()) {
            player.sendTitle(MessageService.colorize(title), "", 0, 40, 10);
        }
    }
}
