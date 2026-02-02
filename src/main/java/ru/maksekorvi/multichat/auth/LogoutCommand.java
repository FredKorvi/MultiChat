package ru.maksekorvi.multichat.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

public class LogoutCommand implements CommandExecutor {
    private final AuthManager authManager;
    private final MessageService messages;

    public LogoutCommand(AuthManager authManager, MessageService messages) {
        this.authManager = authManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messages.send(sender, "errors.player-only");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("multichat.auth.logout")) {
            messages.send(player, "errors.no-permission");
            return true;
        }
        authManager.logout(player);
        messages.send(player, "info.logout-success");
        return true;
    }
}
