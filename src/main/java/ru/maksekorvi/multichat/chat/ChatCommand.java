package ru.maksekorvi.multichat.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.maksekorvi.multichat.util.MessageService;

public class ChatCommand implements CommandExecutor {
    private final ChatManager chatManager;
    private final MessageService messages;

    public ChatCommand(ChatManager chatManager, MessageService messages) {
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
        if (args.length < 1) {
            messages.sendRaw(player, messages.format("errors.usage", "{usage}", "/chat <global|local|guild>"));
            return true;
        }
        ChatChannel channel = ChatChannel.fromId(args[0]);
        chatManager.setChannel(player, channel);
        messages.sendRaw(player, messages.format("info.chat-channel", "{channel}", channel.getId()));
        return true;
    }
}
