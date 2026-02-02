package ru.maksekorvi.multichat.rules;

import org.bukkit.configuration.file.FileConfiguration;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.List;

public class RulesManager {
    private final ConfigManager configManager;
    private final MessageService messages;

    public RulesManager(ConfigManager configManager, MessageService messages) {
        this.configManager = configManager;
        this.messages = messages;
    }

    public List<String> getRules() {
        return configManager.getRules().getStringList("rules");
    }

    public int getPageSize() {
        return configManager.getRules().getInt("pageSize", 5);
    }

    public String getHeader(int page, int max) {
        return messages.format("info.rules-header", "{page}", String.valueOf(page), "{max}", String.valueOf(max));
    }
}
