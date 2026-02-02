package ru.maksekorvi.multichat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.util.MessageService;

public class RootCommand implements CommandExecutor {
    private final MultiChatPlugin plugin;
    private final ConfigManager configManager;
    private final MessageService messages;

    public RootCommand(MultiChatPlugin plugin, ConfigManager configManager, MessageService messages) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("multichat.admin")) {
            messages.send(sender, "errors.no-permission");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            configManager.reloadAll();
            messages.reload();
            plugin.getChatManager().reload();
            plugin.getModerationManager().reload();
            plugin.getGuildManager().reload();
            messages.sendRaw(sender, "&aMultiChat перезагружен.");
            return true;
        }
        messages.sendRaw(sender, "&eИспользование: /multichat reload");
        return true;
    }
}
