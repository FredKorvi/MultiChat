package ru.maksekorvi.multichat.quest;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.guild.GuildManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
    private final ConfigManager configManager;
    private final MessageService messages;
    private final GuildManager guildManager;
    private final Economy economy;
    private final Map<UUID, ActiveQuest> activeQuests = new ConcurrentHashMap<>();
    private final List<QuestDefinition> definitions = new ArrayList<>();
    private final Random random = new Random();

    public QuestManager(ConfigManager configManager, MessageService messages, GuildManager guildManager, Economy economy) {
        this.configManager = configManager;
        this.messages = messages;
        this.guildManager = guildManager;
        this.economy = economy;
        reload();
    }

    public void reload() {
        definitions.clear();
        FileConfiguration cfg = configManager.getQuests();
        List<Map<?, ?>> list = cfg.getMapList("quests");
        for (Map<?, ?> entry : list) {
            String id = String.valueOf(entry.get("id"));
            QuestType type;
            try {
                type = QuestType.valueOf(String.valueOf(entry.get("type")));
            } catch (IllegalArgumentException e) {
                continue;
            }
            String target = String.valueOf(entry.get(type == QuestType.KILL_MOB ? "mob" : type == QuestType.MINE_BLOCK ? "block" : "item"));
            int amount = Integer.parseInt(String.valueOf(entry.get("amount")));
            Map<?, ?> reward = (Map<?, ?>) entry.get("baseReward");
            double money = reward != null && reward.get("money") != null ? Double.parseDouble(String.valueOf(reward.get("money"))) : 0;
            int exp = reward != null && reward.get("exp") != null ? Integer.parseInt(String.valueOf(reward.get("exp"))) : 0;
            int points = reward != null && reward.get("guildPoints") != null ? Integer.parseInt(String.valueOf(reward.get("guildPoints"))) : 0;
            double guildCurrency = reward != null && reward.get("guildCurrency") != null ? Double.parseDouble(String.valueOf(reward.get("guildCurrency"))) : 0;
            definitions.add(new QuestDefinition(id, type, target, amount, money, exp, points, guildCurrency));
        }
    }

    public boolean isQuestEnabled() {
        return configManager.isFeatureEnabled("guilds");
    }

    public void takeQuest(Player player) {
        if (!guildManager.hasGuild(player.getUniqueId())) {
            messages.send(player, "errors.guild-not-found");
            return;
        }
        if (activeQuests.containsKey(player.getUniqueId())) {
            messages.sendRaw(player, "&cУ вас уже есть активный квест.");
            return;
        }
        QuestDefinition definition = getRandomQuest();
        if (definition == null) {
            messages.sendRaw(player, "&cНет доступных квестов.");
            return;
        }
        ActiveQuest quest = new ActiveQuest(definition);
        activeQuests.put(player.getUniqueId(), quest);
        sendQuestInfo(player, quest);
        messages.send(player, "info.guild-quest-take");
    }

    public void refuseQuest(Player player) {
        ActiveQuest quest = activeQuests.remove(player.getUniqueId());
        if (quest == null) {
            messages.send(player, "errors.no-active-quest");
            return;
        }
        guildManager.adjustReliability(player.getUniqueId(), -guildManager.getRefusePenalty());
        messages.send(player, "info.guild-quest-refuse");
        messages.send(player, "info.guild-reliability", "{value}", String.valueOf(guildManager.getReliability(player.getUniqueId())));
    }

    public void sendQuestInfo(Player player) {
        ActiveQuest quest = activeQuests.get(player.getUniqueId());
        if (quest == null) {
            messages.send(player, "errors.no-active-quest");
            return;
        }
        sendQuestInfo(player, quest);
    }

    public void sendQuestInfo(Player player, ActiveQuest quest) {
        QuestDefinition def = quest.getDefinition();
        String description = getDescription(def);
        messages.sendRaw(player, messages.format("info.guild-quest-info", "{quest}", description));
    }

    public void sendQuestProgress(Player player) {
        ActiveQuest quest = activeQuests.get(player.getUniqueId());
        if (quest == null) {
            messages.send(player, "errors.no-active-quest");
            return;
        }
        QuestDefinition def = quest.getDefinition();
        String description = getDescription(def);
        messages.sendRaw(player, messages.format("info.guild-quest-progress",
            "{quest}", description,
            "{progress}", String.valueOf(quest.getProgress()),
            "{amount}", String.valueOf(def.getAmount())));
    }

    public void handleProgress(Player player, QuestType type, String target, int amount) {
        ActiveQuest quest = activeQuests.get(player.getUniqueId());
        if (quest == null) {
            return;
        }
        QuestDefinition def = quest.getDefinition();
        if (def.getType() != type) {
            return;
        }
        if (!def.getTarget().equalsIgnoreCase(target)) {
            return;
        }
        quest.addProgress(amount);
        if (quest.isCompleted()) {
            completeQuest(player, quest);
        }
    }

    private void completeQuest(Player player, ActiveQuest quest) {
        activeQuests.remove(player.getUniqueId());
        QuestDefinition def = quest.getDefinition();
        if (economy != null && def.getRewardMoney() > 0) {
            economy.depositPlayer(player, def.getRewardMoney());
        }
        if (def.getRewardExp() > 0) {
            player.giveExp(def.getRewardExp());
        }
        if (def.getRewardGuildPoints() > 0) {
            guildManager.addGuildPoints(player.getUniqueId(), def.getRewardGuildPoints());
        }
        if (def.getRewardGuildCurrency() > 0) {
            guildManager.addGuildCurrency(player.getUniqueId(), def.getRewardGuildCurrency());
        }
        messages.sendRaw(player, messages.format("info.guild-quest-complete", "{quest}", getDescription(def)));
    }

    private QuestDefinition getRandomQuest() {
        if (definitions.isEmpty()) {
            return null;
        }
        return definitions.get(random.nextInt(definitions.size()));
    }

    private String getDescription(QuestDefinition def) {
        switch (def.getType()) {
            case KILL_MOB:
                return messages.format("quest.description.kill_mob", "{target}", def.getTarget(), "{amount}", String.valueOf(def.getAmount()));
            case MINE_BLOCK:
                return messages.format("quest.description.mine_block", "{target}", def.getTarget(), "{amount}", String.valueOf(def.getAmount()));
            case CRAFT_ITEM:
                return messages.format("quest.description.craft_item", "{target}", def.getTarget(), "{amount}", String.valueOf(def.getAmount()));
            default:
                return def.getId();
        }
    }
}
