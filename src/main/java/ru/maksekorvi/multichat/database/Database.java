package ru.maksekorvi.multichat.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.maksekorvi.multichat.config.ConfigManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

public class Database {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private Connection connection;

    public Database(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void init() {
        try {
            File file = new File(plugin.getDataFolder(), configManager.getConfig().getString("settings.database.file", "multichat.db"));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid TEXT PRIMARY KEY, name TEXT, passhash TEXT, regDate INTEGER, lastIp TEXT, lastLogin INTEGER, sessionUntil INTEGER)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bans (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, ip TEXT, reason TEXT, createdAt INTEGER, expiresAt INTEGER, moderator TEXT)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS mutes (uuid TEXT PRIMARY KEY, reason TEXT, createdAt INTEGER, expiresAt INTEGER, moderator TEXT)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS warns (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, reason TEXT, createdAt INTEGER, moderator TEXT)");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database init failed: " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database: " + e.getMessage());
        }
    }

    public void runAsync(Consumer<Connection> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                consumer.accept(connection);
            } catch (Exception e) {
                plugin.getLogger().warning("Database async error: " + e.getMessage());
            }
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
