package ru.maksekorvi.multichat.guild.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.maksekorvi.multichat.guild.GuildManager;
import ru.maksekorvi.multichat.quest.QuestManager;

import java.util.Arrays;

public class GuildMenu {
    private final GuildManager guildManager;
    private final QuestManager questManager;

    public GuildMenu(GuildManager guildManager, QuestManager questManager) {
        this.guildManager = guildManager;
        this.questManager = questManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.GOLD + "Меню гильдии");
        inv.setItem(10, item(Material.BOOK_AND_QUILL, "&eПравила гильдии", "&7Посмотреть правила"));
        inv.setItem(11, item(Material.SIGN, "&eMOTD гильдии", "&7Посмотреть сообщение дня"));
        inv.setItem(12, item(Material.COMPASS, "&eКвесты гильдии", "&7Текущий квест и прогресс"));
        inv.setItem(14, item(Material.ANVIL, "&eНадежность", "&7Показать вашу надежность"));
        inv.setItem(15, item(Material.BOOK, "&eЧат гильдии", "&7Вкл/выкл чат гильдии"));
        inv.setItem(16, item(Material.INK_SACK, "&eМут участника", "&7Используйте: /g mutechat <ник>"));
        inv.setItem(22, item(Material.PAPER, "&eПомощь", "&7Список команд гильдии"));
        player.openInventory(inv);
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            String[] coloredLore = new String[lore.length];
            for (int i = 0; i < lore.length; i++) {
                coloredLore[i] = ChatColor.translateAlternateColorCodes('&', lore[i]);
            }
            meta.setLore(Arrays.asList(coloredLore));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void handleClick(Player player, int slot) {
        switch (slot) {
            case 10:
                guildManager.sendRules(player);
                break;
            case 11:
                guildManager.sendMotd(player);
                break;
            case 12:
                questManager.sendQuestOverview(player);
                break;
            case 14:
                guildManager.sendReliability(player);
                break;
            case 15:
                guildManager.toggleGuildChat(player);
                break;
            case 16:
                guildManager.sendRawHint(player, "&eЧтобы замутить участника: &6/g mutechat <ник>");
                break;
            case 22:
                guildManager.sendHelp(player);
                break;
            default:
                break;
        }
        player.closeInventory();
    }
}
