package ru.maksekorvi.multichat.broadcast;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.util.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BroadcastManager {
    private final ConfigManager configManager;
    private final MessageService messages;
    private final JavaPlugin plugin;
    private BukkitTask task;
    private int index = 0;
    private final Random random = new Random();

    public BroadcastManager(JavaPlugin plugin, ConfigManager configManager, MessageService messages) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messages = messages;
    }

    public void start() {
        stop();
        FileConfiguration cfg = configManager.getBroadcast();
        if (!cfg.getBoolean("broadcast.enabled", true)) {
            return;
        }
        int interval = cfg.getInt("broadcast.intervalSeconds", 300);
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
            this::broadcastNext, interval * 20L, interval * 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void broadcastNext() {
        List<String> messagesList = getMessages();
        if (messagesList.isEmpty()) {
            return;
        }
        String mode = configManager.getBroadcast().getString("broadcast.mode", "cycle");
        String message;
        if ("random".equalsIgnoreCase(mode)) {
            message = messagesList.get(random.nextInt(messagesList.size()));
        } else {
            if (index >= messagesList.size()) {
                index = 0;
            }
            message = messagesList.get(index++);
        }
        String formatted = MessageService.colorize(message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formatted));
    }

    public List<String> getMessages() {
        return new ArrayList<>(configManager.getBroadcast().getStringList("broadcast.messages"));
    }

    public void addMessage(String message) {
        FileConfiguration cfg = configManager.getBroadcast();
        List<String> list = new ArrayList<>(cfg.getStringList("broadcast.messages"));
        list.add(message);
        cfg.set("broadcast.messages", list);
        configManager.save("broadcast.yml", cfg);
    }

    public void removeMessage(int index) {
        FileConfiguration cfg = configManager.getBroadcast();
        List<String> list = new ArrayList<>(cfg.getStringList("broadcast.messages"));
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            cfg.set("broadcast.messages", list);
            configManager.save("broadcast.yml", cfg);
        }
    }

    public void setEnabled(boolean enabled) {
        FileConfiguration cfg = configManager.getBroadcast();
        cfg.set("broadcast.enabled", enabled);
        configManager.save("broadcast.yml", cfg);
    }

    public void setInterval(int seconds) {
        FileConfiguration cfg = configManager.getBroadcast();
        cfg.set("broadcast.intervalSeconds", seconds);
        configManager.save("broadcast.yml", cfg);
    }
}
