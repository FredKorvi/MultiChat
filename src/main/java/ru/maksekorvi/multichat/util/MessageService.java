package ru.maksekorvi.multichat.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.maksekorvi.multichat.config.ConfigManager;

public class MessageService {
    private final ConfigManager configManager;
    private FileConfiguration messages;

    public MessageService(ConfigManager configManager) {
        this.configManager = configManager;
        reload();
    }

    public void reload() {
        this.messages = configManager.getMessages();
    }

    public void send(CommandSender sender, String path) {
        String prefix = colorize(messages.getString("prefix", ""));
        String msg = colorize(messages.getString(path, ""));
        if (!msg.isEmpty()) {
            sender.sendMessage(prefix + msg);
        }
    }

    public void send(CommandSender sender, String path, String... replacements) {
        String prefix = colorize(messages.getString("prefix", ""));
        String msg = colorize(messages.getString(path, ""));
        msg = applyReplacements(msg, replacements);
        if (!msg.isEmpty()) {
            sender.sendMessage(prefix + msg);
        }
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public String get(String path) {
        return colorize(messages.getString(path, ""));
    }

    public String format(String path, String... replacements) {
        String value = get(path);
        return applyReplacements(value, replacements);
    }

    public String applyReplacements(String value, String... replacements) {
        String result = value;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
