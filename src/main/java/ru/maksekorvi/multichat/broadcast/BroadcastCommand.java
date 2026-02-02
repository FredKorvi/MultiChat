package ru.maksekorvi.multichat.broadcast;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.List;

public class BroadcastCommand implements CommandExecutor {
    private final BroadcastManager manager;
    private final MessageService messages;

    public BroadcastCommand(BroadcastManager manager, MessageService messages) {
        this.manager = manager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("multichat.broadcast.manage")) {
            messages.send(sender, "errors.no-permission");
            return true;
        }
        if (!manager.isFeatureEnabled()) {
            messages.send(sender, "errors.feature-disabled");
            return true;
        }
        if (args.length == 0) {
            messages.sendRaw(sender, "&e/broadcast on|off|interval|list|add|remove|reload");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "on":
                manager.setEnabled(true);
                manager.start();
                messages.sendRaw(sender, "&aБроадкасты включены.");
                return true;
            case "off":
                manager.setEnabled(false);
                manager.stop();
                messages.sendRaw(sender, "&cБроадкасты отключены.");
                return true;
            case "interval":
                if (args.length < 2) {
                    messages.sendRaw(sender, "&cИспользование: /broadcast interval <seconds>");
                    return true;
                }
                int seconds = Integer.parseInt(args[1]);
                manager.setInterval(seconds);
                manager.start();
                messages.sendRaw(sender, "&aИнтервал обновлен.");
                return true;
            case "list":
                List<String> list = manager.getMessages();
                messages.send(sender, "info.broadcast-list");
                for (int i = 0; i < list.size(); i++) {
                    messages.sendRaw(sender, "&e" + i + ": &f" + MessageService.colorize(list.get(i)));
                }
                return true;
            case "add":
                if (args.length < 2) {
                    messages.sendRaw(sender, "&cИспользование: /broadcast add <text>");
                    return true;
                }
                String text = String.join(" ", args).substring(args[0].length()).trim();
                manager.addMessage(text);
                messages.send(sender, "info.broadcast-added", "{id}", String.valueOf(manager.getMessages().size() - 1));
                return true;
            case "remove":
                if (args.length < 2) {
                    messages.sendRaw(sender, "&cИспользование: /broadcast remove <id>");
                    return true;
                }
                int id = Integer.parseInt(args[1]);
                manager.removeMessage(id);
                messages.send(sender, "info.broadcast-removed", "{id}", args[1]);
                return true;
            case "reload":
                manager.start();
                messages.sendRaw(sender, "&aБроадкасты перезагружены.");
                return true;
            default:
                messages.sendRaw(sender, "&e/broadcast on|off|interval|list|add|remove|reload");
                return true;
        }
    }
}
