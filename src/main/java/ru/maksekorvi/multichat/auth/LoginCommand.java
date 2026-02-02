package ru.maksekorvi.multichat.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

public class LoginCommand implements CommandExecutor {
    private final AuthManager authManager;
    private final MessageService messages;

    public LoginCommand(AuthManager authManager, MessageService messages) {
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
        if (!player.hasPermission("multichat.auth.login")) {
            messages.send(player, "errors.no-permission");
            return true;
        }
        if (!authManager.isRegistered(player)) {
            messages.send(player, "errors.not-registered");
            return true;
        }
        if (authManager.isAuthenticated(player)) {
            messages.send(player, "errors.already-logged");
            return true;
        }
        if (args.length < 1) {
            messages.sendRaw(player, messages.format("errors.usage", "{usage}", "/login <password>"));
            return true;
        }
        authManager.login(player, args[0]);
        return true;
    }
}
