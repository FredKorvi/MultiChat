package ru.maksekorvi.multichat.rules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.List;

public class RulesCommand implements CommandExecutor {
    private final RulesManager rulesManager;
    private final MessageService messages;

    public RulesCommand(RulesManager rulesManager, MessageService messages) {
        this.rulesManager = rulesManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!rulesManager.isFeatureEnabled()) {
            messages.send(sender, "errors.feature-disabled");
            return true;
        }
        List<String> rules = rulesManager.getRules();
        int pageSize = rulesManager.getPageSize();
        int maxPage = Math.max(1, (int) Math.ceil((double) rules.size() / pageSize));
        int page = 1;
        if (args.length > 0) {
            page = Math.max(1, Math.min(maxPage, Integer.parseInt(args[0])));
        }
        sender.sendMessage(rulesManager.getHeader(page, maxPage));
        int start = (page - 1) * pageSize;
        int end = Math.min(rules.size(), start + pageSize);
        for (int i = start; i < end; i++) {
            messages.sendRaw(sender, rules.get(i));
        }
        return true;
    }
}
