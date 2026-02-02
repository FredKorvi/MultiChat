package ru.maksekorvi.multichat.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

public class RegisterCommand implements CommandExecutor {
    private final AuthManager authManager;
    private final MessageService messages;

    public RegisterCommand(AuthManager authManager, MessageService messages) {
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
        if (!authManager.isAuthEnabled()) {
            messages.send(player, "errors.feature-disabled");
            return true;
        }
        if (!player.hasPermission("multichat.auth.register")) {
            messages.send(player, "errors.no-permission");
            return true;
        }
        if (authManager.isRegistered(player)) {
            messages.send(player, "errors.already-registered");
            return true;
        }
        if (args.length < 2) {
            messages.sendRaw(player, messages.format("errors.usage", "{usage}", "/register <password> <password>"));
            return true;
        }
        if (!args[0].equals(args[1])) {
            messages.send(player, "errors.passwords-mismatch");
            return true;
        }
        authManager.register(player, args[0]);
        return true;
    }
}
