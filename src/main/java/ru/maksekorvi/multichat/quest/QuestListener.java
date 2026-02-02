package ru.maksekorvi.multichat.quest;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class QuestListener implements Listener {
    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player player = event.getEntity().getKiller();
        EntityType type = event.getEntityType();
        questManager.handleProgress(player, QuestType.KILL_MOB, type.name(), 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        questManager.handleProgress(player, QuestType.MINE_BLOCK, material.name(), 1);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) {
            return;
        }
        Material material = event.getCurrentItem().getType();
        int amount = event.getCurrentItem().getAmount();
        questManager.handleProgress(player, QuestType.CRAFT_ITEM, material.name(), amount);
    }
}
