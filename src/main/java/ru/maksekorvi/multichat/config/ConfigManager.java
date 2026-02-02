package ru.maksekorvi.multichat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration auth;
    private FileConfiguration chat;
    private FileConfiguration broadcast;
    private FileConfiguration guilds;
    private FileConfiguration quests;
    private FileConfiguration moderation;
    private FileConfiguration rules;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadAll();
    }

    public void reloadAll() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        messages = load("messages.yml");
        auth = load("auth.yml");
        chat = load("chat.yml");
        broadcast = load("broadcast.yml");
        guilds = load("guilds.yml");
        quests = load("quests.yml");
        moderation = load("moderation.yml");
        rules = load("rules.yml");
    }

    private FileConfiguration load(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(String name, FileConfiguration configuration) {
        File file = new File(plugin.getDataFolder(), name);
        try {
            configuration.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getAuth() {
        return auth;
    }

    public FileConfiguration getChat() {
        return chat;
    }

    public FileConfiguration getBroadcast() {
        return broadcast;
    }

    public FileConfiguration getGuilds() {
        return guilds;
    }

    public FileConfiguration getQuests() {
        return quests;
    }

    public FileConfiguration getModeration() {
        return moderation;
    }

    public FileConfiguration getRules() {
        return rules;
    }
}
