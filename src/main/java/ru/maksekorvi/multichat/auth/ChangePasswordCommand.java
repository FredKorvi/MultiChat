package ru.maksekorvi.multichat.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

public class ChangePasswordCommand implements CommandExecutor {
    private final AuthManager authManager;
    private final MessageService messages;

    public ChangePasswordCommand(AuthManager authManager, MessageService messages) {
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
        if (!player.hasPermission("multichat.auth.changepass")) {
            messages.send(player, "errors.no-permission");
            return true;
        }
        if (args.length < 3) {
            messages.sendRaw(player, messages.format("errors.usage", "{usage}", "/changepass <old> <new> <new>"));
            return true;
        }
        if (!args[1].equals(args[2])) {
            messages.send(player, "errors.passwords-mismatch");
            return true;
        }
        authManager.changePassword(player, args[0], args[1]);
        return true;
    }
}
