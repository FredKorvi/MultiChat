package ru.maksekorvi.multichat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.maksekorvi.multichat.auth.AuthListener;
import ru.maksekorvi.multichat.auth.AuthManager;
import ru.maksekorvi.multichat.broadcast.BroadcastCommand;
import ru.maksekorvi.multichat.broadcast.BroadcastManager;
import ru.maksekorvi.multichat.chat.ChatListener;
import ru.maksekorvi.multichat.chat.ChatManager;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.database.Database;
import ru.maksekorvi.multichat.guild.GuildCommand;
import ru.maksekorvi.multichat.guild.GuildManager;
import ru.maksekorvi.multichat.moderation.ModerationCommand;
import ru.maksekorvi.multichat.moderation.ModerationListener;
import ru.maksekorvi.multichat.moderation.ModerationManager;
import ru.maksekorvi.multichat.placeholder.MultiChatExpansion;
import ru.maksekorvi.multichat.quest.QuestListener;
import ru.maksekorvi.multichat.quest.QuestManager;
import ru.maksekorvi.multichat.rules.RulesCommand;
import ru.maksekorvi.multichat.rules.RulesManager;
import ru.maksekorvi.multichat.util.MessageService;

public class MultiChatPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private MessageService messageService;
    private Database database;
    private AuthManager authManager;
    private ChatManager chatManager;
    private BroadcastManager broadcastManager;
    private GuildManager guildManager;
    private ModerationManager moderationManager;
    private RulesManager rulesManager;
    private QuestManager questManager;

    private Economy economy;
    private Chat vaultChat;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.messageService = new MessageService(configManager);
        this.database = new Database(this, configManager);
        database.init();

        setupVault();

        this.authManager = new AuthManager(this, database, configManager, messageService);
        this.guildManager = new GuildManager(this, configManager, messageService, economy);
        this.chatManager = new ChatManager(this, configManager, messageService, guildManager, vaultChat);
        this.broadcastManager = new BroadcastManager(this, configManager, messageService);
        this.moderationManager = new ModerationManager(this, database, configManager, messageService);
        this.rulesManager = new RulesManager(configManager, messageService);
        this.questManager = new QuestManager(configManager, messageService, guildManager, economy);

        registerCommands();
        registerListeners();

        broadcastManager.start();
        authManager.loadOnlineSessions();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MultiChatExpansion(this, chatManager, guildManager).register();
        }
    }

    @Override
    public void onDisable() {
        broadcastManager.stop();
        database.shutdown();
    }

    private void registerCommands() {
        register(getCommand("register"), authManager.getRegisterCommand());
        register(getCommand("login"), authManager.getLoginCommand());
        register(getCommand("changepass"), authManager.getChangePasswordCommand());
        register(getCommand("logout"), authManager.getLogoutCommand());
        register(getCommand("broadcast"), new BroadcastCommand(broadcastManager, messageService));
        register(getCommand("g"), new GuildCommand(guildManager, chatManager, questManager, messageService));
        register(getCommand("rules"), new RulesCommand(rulesManager, messageService));
        register(getCommand("multichat"), new RootCommand(this, configManager, messageService));
        ModerationCommand moderationCommand = new ModerationCommand(moderationManager, messageService);
        register(getCommand("ban"), moderationCommand);
        register(getCommand("unban"), moderationCommand);
        register(getCommand("tempban"), moderationCommand);
        register(getCommand("ban-ip"), moderationCommand);
        register(getCommand("mute"), moderationCommand);
        register(getCommand("unmute"), moderationCommand);
        register(getCommand("freeze"), moderationCommand);
        register(getCommand("unfreeze"), moderationCommand);
        register(getCommand("kick"), moderationCommand);
        register(getCommand("warn"), moderationCommand);
        register(getCommand("unwarn"), moderationCommand);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new AuthListener(authManager, messageService), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(chatManager, authManager, messageService), this);
        Bukkit.getPluginManager().registerEvents(new ModerationListener(moderationManager, messageService), this);
        Bukkit.getPluginManager().registerEvents(new QuestListener(questManager), this);
    }

    private void register(PluginCommand command, org.bukkit.command.CommandExecutor executor) {
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    private void setupVault() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            vaultChat = chatProvider.getProvider();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }
}
