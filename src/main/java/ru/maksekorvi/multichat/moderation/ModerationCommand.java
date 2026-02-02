package ru.maksekorvi.multichat.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

import java.net.InetSocketAddress;
import java.time.Instant;

public class ModerationCommand implements CommandExecutor {
    private final ModerationManager moderationManager;
    private final MessageService messages;

    public ModerationCommand(ModerationManager moderationManager, MessageService messages) {
        this.moderationManager = moderationManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if (!sender.hasPermission("multichat.mod." + name)) {
            messages.send(sender, "errors.no-permission");
            return true;
        }
        if (!moderationManager.isFeatureEnabled()) {
            messages.send(sender, "errors.feature-disabled");
            return true;
        }
        switch (name) {
            case "ban":
                return handleBan(sender, args, 0);
            case "tempban":
                return handleTempBan(sender, args);
            case "ban-ip":
                return handleBanIp(sender, args);
            case "unban":
                return handleUnban(sender, args);
            case "mute":
                return handleMute(sender, args);
            case "unmute":
                return handleUnmute(sender, args);
            case "freeze":
                return handleFreeze(sender, args);
            case "unfreeze":
                return handleUnfreeze(sender, args);
            case "kick":
                return handleKick(sender, args);
            case "warn":
                return handleWarn(sender, args);
            case "unwarn":
                return handleUnwarn(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBan(CommandSender sender, String[] args, long expiresAt) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /ban <player> [reason]");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String reason = args.length > 1 ? joinReason(args, 1) : moderationManager.getDefaultBanReason();
        moderationManager.ban(target.getUniqueId(), null, reason, expiresAt, sender.getName());
        if (target.isOnline()) {
            ((Player) target).kickPlayer(reason);
        }
        messages.sendRaw(sender, "&aИгрок забанен.");
        return true;
    }

    private boolean handleTempBan(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.sendRaw(sender, "&cИспользование: /tempban <player> <minutes> [reason]");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        long minutes = Long.parseLong(args[1]);
        long expiresAt = Instant.now().toEpochMilli() + minutes * 60_000L;
        String reason = args.length > 2 ? joinReason(args, 2) : moderationManager.getDefaultBanReason();
        moderationManager.ban(target.getUniqueId(), null, reason, expiresAt, sender.getName());
        if (target.isOnline()) {
            ((Player) target).kickPlayer(reason);
        }
        messages.sendRaw(sender, "&aИгрок забанен временно.");
        return true;
    }

    private boolean handleBanIp(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /ban-ip <player|ip> [reason]");
            return true;
        }
        String reason = args.length > 1 ? joinReason(args, 1) : moderationManager.getDefaultBanReason();
        Player online = Bukkit.getPlayer(args[0]);
        if (online != null) {
            InetSocketAddress address = online.getAddress();
            String ip = address != null ? address.getAddress().getHostAddress() : null;
            moderationManager.ban(null, ip, reason, 0, sender.getName());
            online.kickPlayer(reason);
        } else {
            moderationManager.ban(null, args[0], reason, 0, sender.getName());
        }
        messages.sendRaw(sender, "&aIP забанен.");
        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /unban <player>" );
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        moderationManager.unban(target.getUniqueId(), null);
        messages.sendRaw(sender, "&aБан снят.");
        return true;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /mute <player> [minutes] [reason]");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        long expiresAt = 0;
        int reasonIndex = 1;
        if (args.length >= 2 && args[1].matches("\\d+")) {
            long minutes = Long.parseLong(args[1]);
            expiresAt = Instant.now().toEpochMilli() + minutes * 60_000L;
            reasonIndex = 2;
        }
        String reason = args.length > reasonIndex ? joinReason(args, reasonIndex) : moderationManager.getDefaultMuteReason();
        moderationManager.mute(target.getUniqueId(), reason, expiresAt, sender.getName());
        messages.sendRaw(sender, "&aИгрок замучен.");
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /unmute <player>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        moderationManager.unmute(target.getUniqueId());
        messages.sendRaw(sender, "&aМут снят.");
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /freeze <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(sender, "errors.player-not-found");
            return true;
        }
        moderationManager.freeze(target);
        messages.sendRaw(sender, "&aИгрок заморожен.");
        return true;
    }

    private boolean handleUnfreeze(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /unfreeze <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(sender, "errors.player-not-found");
            return true;
        }
        moderationManager.unfreeze(target);
        messages.sendRaw(sender, "&aИгрок разморожен.");
        return true;
    }

    private boolean handleKick(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /kick <player> [reason]");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(sender, "errors.player-not-found");
            return true;
        }
        String reason = args.length > 1 ? joinReason(args, 1) : moderationManager.getDefaultBanReason();
        target.kickPlayer(reason);
        messages.sendRaw(sender, "&aИгрок кикнут.");
        return true;
    }

    private boolean handleWarn(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /warn <player> [reason]");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String reason = args.length > 1 ? joinReason(args, 1) : moderationManager.getDefaultWarnReason();
        moderationManager.warn(target.getUniqueId(), reason, sender.getName());
        if (target.isOnline()) {
            messages.send((Player) target, "info.moderation-warn", "{reason}", reason);
        }
        messages.sendRaw(sender, "&aПредупреждение выдано.");
        return true;
    }

    private boolean handleUnwarn(CommandSender sender, String[] args) {
        if (args.length < 1) {
            messages.sendRaw(sender, "&cИспользование: /unwarn <player> [index]" );
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        int index = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        moderationManager.removeWarn(target.getUniqueId(), index);
        messages.sendRaw(sender, "&aПредупреждение снято.");
        return true;
    }

    private String joinReason(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
