package ru.maksekorvi.multichat.guild;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageService messages;
    private final Economy economy;
    private final Map<String, Guild> guilds = new ConcurrentHashMap<>();
    private final Map<UUID, String> memberGuild = new ConcurrentHashMap<>();
    private final Map<UUID, String> invites = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> activeQuest = new ConcurrentHashMap<>();
    private final Set<UUID> guildChat = ConcurrentHashMap.newKeySet();
    private File dataFile;
    private FileConfiguration dataConfig;

    public GuildManager(JavaPlugin plugin, ConfigManager configManager, MessageService messages, Economy economy) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messages = messages;
        this.economy = economy;
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "guild-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create guild-data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        guilds.clear();
        memberGuild.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("guilds");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                int level = section.getInt(key + ".level", 1);
                int points = section.getInt(key + ".points", 0);
                double currency = section.getDouble(key + ".currency", 0);
                Map<UUID, Integer> members = new HashMap<>();
                Map<UUID, Integer> reliability = new HashMap<>();
                ConfigurationSection membersSec = section.getConfigurationSection(key + ".members");
                if (membersSec != null) {
                    for (String uuidStr : membersSec.getKeys(false)) {
                        UUID uuid = UUID.fromString(uuidStr);
                        int rank = membersSec.getInt(uuidStr);
                        members.put(uuid, rank);
                        memberGuild.put(uuid, name.toLowerCase());
                    }
                }
                ConfigurationSection reliabilitySec = section.getConfigurationSection(key + ".reliability");
                if (reliabilitySec != null) {
                    for (String uuidStr : reliabilitySec.getKeys(false)) {
                        reliability.put(UUID.fromString(uuidStr), reliabilitySec.getInt(uuidStr));
                    }
                }
                Guild guild = new Guild(name, level, points, currency, members, reliability);
                guilds.put(name.toLowerCase(), guild);
            }
        }
    }

    private void save() {
        dataConfig.set("guilds", null);
        for (Guild guild : guilds.values()) {
            String base = "guilds." + guild.getName().toLowerCase();
            dataConfig.set(base + ".name", guild.getName());
            dataConfig.set(base + ".level", guild.getLevel());
            dataConfig.set(base + ".points", guild.getGuildPoints());
            dataConfig.set(base + ".currency", guild.getGuildCurrency());
            for (Map.Entry<UUID, Integer> entry : guild.getMembers().entrySet()) {
                dataConfig.set(base + ".members." + entry.getKey().toString(), entry.getValue());
            }
            for (Map.Entry<UUID, Integer> entry : guild.getReliability().entrySet()) {
                dataConfig.set(base + ".reliability." + entry.getKey().toString(), entry.getValue());
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save guild-data.yml: " + e.getMessage());
        }
    }

    public boolean hasGuild(UUID uuid) {
        return memberGuild.containsKey(uuid);
    }

    public String getGuildName(UUID uuid) {
        String name = memberGuild.get(uuid);
        if (name == null) {
            return null;
        }
        Guild guild = guilds.get(name);
        return guild != null ? guild.getName() : null;
    }

    public String getGuildRankName(UUID uuid) {
        String guildName = memberGuild.get(uuid);
        if (guildName == null) {
            return "-";
        }
        Guild guild = guilds.get(guildName);
        if (guild == null) {
            return "-";
        }
        int rankId = guild.getMembers().getOrDefault(uuid, 1);
        String path = "ranks." + rankId + ".name";
        return configManager.getGuilds().getString(path, "-");
    }

    public int getGuildRankId(UUID uuid) {
        String guildName = memberGuild.get(uuid);
        if (guildName == null) {
            return 0;
        }
        Guild guild = guilds.get(guildName);
        if (guild == null) {
            return 0;
        }
        return guild.getMembers().getOrDefault(uuid, 1);
    }

    public int getGuildLevel(UUID uuid) {
        String guildName = memberGuild.get(uuid);
        if (guildName == null) {
            return 0;
        }
        Guild guild = guilds.get(guildName);
        return guild != null ? guild.getLevel() : 0;
    }

    public int getGuildPoints(UUID uuid) {
        String guildName = memberGuild.get(uuid);
        if (guildName == null) {
            return 0;
        }
        Guild guild = guilds.get(guildName);
        return guild != null ? guild.getGuildPoints() : 0;
    }

    public double getGuildCurrency(UUID uuid) {
        String guildName = memberGuild.get(uuid);
        if (guildName == null) {
            return 0;
        }
        Guild guild = guilds.get(guildName);
        return guild != null ? guild.getGuildCurrency() : 0;
    }

    public void sendGuildMessage(UUID sender, String message) {
        String name = memberGuild.get(sender);
        if (name == null) {
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            return;
        }
        for (UUID member : guild.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(member);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public void createGuild(Player player, String name) {
        if (hasGuild(player.getUniqueId())) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (guilds.containsKey(name.toLowerCase())) {
            messages.sendRaw(player, "&cГильдия уже существует.");
            return;
        }
        Map<UUID, Integer> members = new HashMap<>();
        members.put(player.getUniqueId(), 5);
        Map<UUID, Integer> reliability = new HashMap<>();
        reliability.put(player.getUniqueId(), getStartReliability());
        Guild guild = new Guild(name, 1, 0, 0, members, reliability);
        guilds.put(name.toLowerCase(), guild);
        memberGuild.put(player.getUniqueId(), name.toLowerCase());
        save();
        messages.send(player, "info.guild-created", "{guild}", name);
    }

    public void disbandGuild(Player player) {
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (guild.getMembers().getOrDefault(player.getUniqueId(), 1) < 5) {
            messages.send(player, "errors.guild-no-permission");
            return;
        }
        for (UUID member : guild.getMembers().keySet()) {
            memberGuild.remove(member);
        }
        guilds.remove(name);
        save();
        messages.send(player, "info.guild-disbanded");
    }

    public void invite(Player player, Player target) {
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        invites.put(target.getUniqueId(), name);
        messages.sendRaw(target, "&eВас пригласили в гильдию " + guild.getName() + ". Используйте /g accept " + guild.getName());
        messages.sendRaw(player, "&aПриглашение отправлено.");
    }

    public void acceptInvite(Player player, String guildName) {
        String invited = invites.get(player.getUniqueId());
        if (invited == null || !invited.equalsIgnoreCase(guildName)) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(invited);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        guild.getMembers().put(player.getUniqueId(), 1);
        guild.getReliability().put(player.getUniqueId(), getStartReliability());
        memberGuild.put(player.getUniqueId(), invited);
        invites.remove(player.getUniqueId());
        save();
        messages.send(player, "info.guild-joined", "{guild}", guild.getName());
    }

    public void leave(Player player) {
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (guild.getMembers().getOrDefault(player.getUniqueId(), 1) >= 5) {
            messages.sendRaw(player, "&cЛидер не может выйти, используйте /g disband.");
            return;
        }
        guild.getMembers().remove(player.getUniqueId());
        guild.getReliability().remove(player.getUniqueId());
        memberGuild.remove(player.getUniqueId());
        save();
        messages.send(player, "info.guild-left");
    }

    public void kick(Player player, Player target) {
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (!guild.getMembers().containsKey(target.getUniqueId())) {
            messages.send(player, "errors.player-not-found");
            return;
        }
        if (guild.getMembers().getOrDefault(player.getUniqueId(), 1) < 3) {
            messages.send(player, "errors.guild-no-permission");
            return;
        }
        guild.getMembers().remove(target.getUniqueId());
        guild.getReliability().remove(target.getUniqueId());
        memberGuild.remove(target.getUniqueId());
        save();
        messages.sendRaw(player, "&aИгрок исключен.");
        messages.sendRaw(target, "&cВас исключили из гильдии.");
    }

    public void deposit(Player player, double amount) {
        if (economy == null) {
            messages.sendRaw(player, "&cVault экономика не найдена.");
            return;
        }
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (!economy.has(player, amount)) {
            messages.sendRaw(player, "&cНедостаточно средств.");
            return;
        }
        economy.withdrawPlayer(player, amount);
        guild.addGuildCurrency(amount);
        save();
        messages.sendRaw(player, "&aПополнение банка выполнено.");
    }

    public void withdraw(Player player, double amount) {
        if (economy == null) {
            messages.sendRaw(player, "&cVault экономика не найдена.");
            return;
        }
        String name = memberGuild.get(player.getUniqueId());
        if (name == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (guild.getGuildCurrency() < amount) {
            messages.sendRaw(player, "&cНедостаточно средств в банке гильдии.");
            return;
        }
        guild.addGuildCurrency(-amount);
        economy.depositPlayer(player, amount);
        save();
        messages.sendRaw(player, "&aСнятие выполнено.");
    }

    public void bonus(Player sender, Player target, int amount) {
        if (economy == null) {
            messages.sendRaw(sender, "&cVault экономика не найдена.");
            return;
        }
        String name = memberGuild.get(sender.getUniqueId());
        if (name == null) {
            messages.send(sender, "errors.guild-not-found");
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            messages.send(sender, "errors.guild-not-found");
            return;
        }
        if (guild.getGuildCurrency() < amount) {
            messages.sendRaw(sender, "&cНедостаточно средств в банке.");
            return;
        }
        guild.addGuildCurrency(-amount);
        economy.depositPlayer(target, amount);
        save();
        messages.sendRaw(sender, "&aПремия выдана.");
        messages.sendRaw(target, "&aВы получили премию " + amount + ".");
    }

    public List<Guild> getTopGuilds() {
        List<Guild> list = new ArrayList<>(guilds.values());
        list.sort(Comparator.comparingInt(Guild::getGuildPoints).reversed());
        return list;
    }

    public void toggleGuildChat(Player player) {
        if (guildChat.contains(player.getUniqueId())) {
            guildChat.remove(player.getUniqueId());
            messages.send(player, "info.guild-chat-toggle", "{state}", "&cOFF");
        } else {
            guildChat.add(player.getUniqueId());
            messages.send(player, "info.guild-chat-toggle", "{state}", "&aON");
        }
    }

    public boolean isGuildChatEnabled(UUID uuid) {
        return guildChat.contains(uuid);
    }

    public void takeQuest(Player player) {
        if (!hasGuild(player.getUniqueId())) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (activeQuest.getOrDefault(player.getUniqueId(), false)) {
            messages.sendRaw(player, "&cУ вас уже есть активный квест.");
            return;
        }
        activeQuest.put(player.getUniqueId(), true);
        messages.send(player, "info.guild-quest-take");
    }

    public void refuseQuest(Player player) {
        if (!hasGuild(player.getUniqueId())) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (!activeQuest.getOrDefault(player.getUniqueId(), false)) {
            messages.sendRaw(player, "&cУ вас нет активного квеста.");
            return;
        }
        activeQuest.put(player.getUniqueId(), false);
        int reliability = Math.max(0, getReliability(player.getUniqueId()) - getRefusePenalty());
        setReliability(player.getUniqueId(), reliability);
        save();
        messages.send(player, "info.guild-quest-refuse");
        messages.send(player, "info.guild-reliability", "{value}", String.valueOf(reliability));
    }

    public int getReliability(UUID uuid) {
        String name = memberGuild.get(uuid);
        if (name == null) {
            return 0;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            return 0;
        }
        return guild.getReliability().getOrDefault(uuid, getStartReliability());
    }

    private void setReliability(UUID uuid, int value) {
        String name = memberGuild.get(uuid);
        if (name == null) {
            return;
        }
        Guild guild = guilds.get(name);
        if (guild == null) {
            return;
        }
        guild.getReliability().put(uuid, value);
    }

    private int getStartReliability() {
        return configManager.getGuilds().getInt("settings.reliability.start", 100);
    }

    private int getRefusePenalty() {
        return configManager.getGuilds().getInt("settings.reliability.refusePenalty", 10);
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isFeatureEnabled() {
        return configManager.isFeatureEnabled("guilds");
    }
}
