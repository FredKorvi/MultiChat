package ru.maksekorvi.multichat.guild;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.chat.ChatChannel;
import ru.maksekorvi.multichat.chat.ChatManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.List;

public class GuildCommand implements CommandExecutor {
    private final GuildManager guildManager;
    private final ChatManager chatManager;
    private final MessageService messages;

    public GuildCommand(GuildManager guildManager, ChatManager chatManager, MessageService messages) {
        this.guildManager = guildManager;
        this.chatManager = chatManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messages.send(sender, "errors.player-only");
            return true;
        }
        Player player = (Player) sender;
        if (!guildManager.isFeatureEnabled()) {
            messages.send(player, "errors.feature-disabled");
            return true;
        }
        if (args.length == 0) {
            messages.sendRaw(player, "&6Гильдии: &e/g create|disband|invite|accept|kick|leave|info|top|bank|bonus|chat|takequest|refusequest");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    messages.sendRaw(player, "&cИспользование: &e/g create <name>");
                    return true;
                }
                guildManager.createGuild(player, args[1]);
                return true;
            case "disband":
                guildManager.disbandGuild(player);
                return true;
            case "invite":
                if (args.length < 2) {
                    messages.sendRaw(player, "&cИспользование: &e/g invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    messages.send(player, "errors.player-not-found");
                    return true;
                }
                guildManager.invite(player, target);
                return true;
            case "accept":
                if (args.length < 2) {
                    messages.sendRaw(player, "&cИспользование: &e/g accept <guild>");
                    return true;
                }
                guildManager.acceptInvite(player, args[1]);
                return true;
            case "kick":
                if (args.length < 2) {
                    messages.sendRaw(player, "&cИспользование: &e/g kick <player>");
                    return true;
                }
                Player kickTarget = Bukkit.getPlayer(args[1]);
                if (kickTarget == null) {
                    messages.send(player, "errors.player-not-found");
                    return true;
                }
                guildManager.kick(player, kickTarget);
                return true;
            case "leave":
                guildManager.leave(player);
                return true;
            case "info":
                String guild = guildManager.getGuildName(player.getUniqueId());
                if (guild == null) {
                    messages.send(player, "errors.guild-not-found");
                    return true;
                }
                messages.sendRaw(player, "&eГильдия: " + guild);
                messages.sendRaw(player, "&eУровень: " + guildManager.getGuildLevel(player.getUniqueId()));
                messages.sendRaw(player, "&eОчки: " + guildManager.getGuildPoints(player.getUniqueId()));
                messages.sendRaw(player, "&eБанк: " + guildManager.getGuildCurrency(player.getUniqueId()));
                return true;
            case "top":
                List<Guild> top = guildManager.getTopGuilds();
                messages.sendRaw(player, "&eТоп гильдий:");
                for (int i = 0; i < Math.min(10, top.size()); i++) {
                    Guild g = top.get(i);
                    messages.sendRaw(player, "&e" + (i + 1) + ". " + g.getName() + " - " + g.getGuildPoints());
                }
                return true;
            case "bank":
                if (args.length < 2) {
                    messages.sendRaw(player, "&cИспользование: &e/g bank balance|deposit|withdraw <amount>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("balance")) {
                    messages.sendRaw(player, "&eБанк гильдии: " + guildManager.getGuildCurrency(player.getUniqueId()));
                    return true;
                }
                if (args.length < 3) {
                    messages.sendRaw(player, "&cИспользование: &e/g bank deposit|withdraw <amount>");
                    return true;
                }
                double amount = Double.parseDouble(args[2]);
                if (args[1].equalsIgnoreCase("deposit")) {
                    guildManager.deposit(player, amount);
                } else if (args[1].equalsIgnoreCase("withdraw")) {
                    guildManager.withdraw(player, amount);
                }
                return true;
            case "bonus":
                if (args.length < 3) {
                    messages.sendRaw(player, "&cИспользование: &e/g bonus <player> <amount>");
                    return true;
                }
                Player bonusTarget = Bukkit.getPlayer(args[1]);
                if (bonusTarget == null) {
                    messages.send(player, "errors.player-not-found");
                    return true;
                }
                int bonus = Integer.parseInt(args[2]);
                guildManager.bonus(player, bonusTarget, bonus);
                return true;
            case "chat":
                guildManager.toggleGuildChat(player);
                if (guildManager.isGuildChatEnabled(player.getUniqueId())) {
                    chatManager.setChannel(player, ChatChannel.GUILD);
                } else {
                    chatManager.setChannel(player, ChatChannel.LOCAL);
                }
                return true;
            case "takequest":
                guildManager.takeQuest(player);
                return true;
            case "refusequest":
                guildManager.refuseQuest(player);
                return true;
            default:
                messages.sendRaw(player, "&6Гильдии: &e/g create|disband|invite|accept|kick|leave|info|top|bank|bonus|chat|takequest|refusequest");
                return true;
        }
    }
}
