package ru.maksekorvi.multichat.guild.menu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuildMenuListener implements Listener {
    private final GuildMenu menu;

    public GuildMenuListener(GuildMenu menu) {
        this.menu = menu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Меню гильдии")) {
            return;
        }
        event.setCancelled(true);
        menu.handleClick((Player) event.getWhoClicked(), event.getRawSlot());
    }
}
