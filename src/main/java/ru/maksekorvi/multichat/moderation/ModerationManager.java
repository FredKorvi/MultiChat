package ru.maksekorvi.multichat.moderation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.database.Database;
import ru.maksekorvi.multichat.util.MessageService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModerationManager {
    private final Database database;
    private final ConfigManager configManager;
    private final MessageService messages;
    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();

    public ModerationManager(JavaPlugin plugin, Database database, ConfigManager configManager, MessageService messages) {
        this.database = database;
        this.configManager = configManager;
        this.messages = messages;
    }

    public void reload() {
        // Config is reloaded by ConfigManager
    }

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    public void freeze(Player player) {
        frozen.add(player.getUniqueId());
        messages.send(player, "errors.moderation-frozen");
    }

    public void unfreeze(Player player) {
        frozen.remove(player.getUniqueId());
    }

    public void ban(UUID uuid, String ip, String reason, long expiresAt, String moderator) {
        long now = Instant.now().toEpochMilli();
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bans (uuid, ip, reason, createdAt, expiresAt, moderator) VALUES (?,?,?,?,?,?)")) {
                stmt.setString(1, uuid != null ? uuid.toString() : null);
                stmt.setString(2, ip);
                stmt.setString(3, reason);
                stmt.setLong(4, now);
                stmt.setLong(5, expiresAt);
                stmt.setString(6, moderator);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to ban: " + e.getMessage());
            }
        });
    }

    public void unban(UUID uuid, String ip) {
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bans WHERE uuid = ? OR ip = ?")) {
                stmt.setString(1, uuid != null ? uuid.toString() : null);
                stmt.setString(2, ip);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to unban: " + e.getMessage());
            }
        });
    }

    public BanRecord getActiveBan(UUID uuid, String ip) {
        try {
            PreparedStatement stmt = database.getConnection().prepareStatement("SELECT * FROM bans WHERE (uuid = ? OR ip = ?) ORDER BY createdAt DESC LIMIT 1");
            stmt.setString(1, uuid != null ? uuid.toString() : "");
            stmt.setString(2, ip != null ? ip : "");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long expiresAt = rs.getLong("expiresAt");
                if (expiresAt == 0 || expiresAt > Instant.now().toEpochMilli()) {
                    return new BanRecord(rs.getString("reason"), expiresAt);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Failed to read ban: " + e.getMessage());
        }
        return null;
    }

    public void mute(UUID uuid, String reason, long expiresAt, String moderator) {
        long now = Instant.now().toEpochMilli();
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO mutes (uuid, reason, createdAt, expiresAt, moderator) VALUES (?,?,?,?,?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, reason);
                stmt.setLong(3, now);
                stmt.setLong(4, expiresAt);
                stmt.setString(5, moderator);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to mute: " + e.getMessage());
            }
        });
    }

    public void unmute(UUID uuid) {
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM mutes WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to unmute: " + e.getMessage());
            }
        });
    }

    public MuteRecord getActiveMute(UUID uuid) {
        try {
            PreparedStatement stmt = database.getConnection().prepareStatement("SELECT * FROM mutes WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long expiresAt = rs.getLong("expiresAt");
                if (expiresAt == 0 || expiresAt > Instant.now().toEpochMilli()) {
                    return new MuteRecord(rs.getString("reason"), expiresAt);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Failed to read mute: " + e.getMessage());
        }
        return null;
    }

    public void warn(UUID uuid, String reason, String moderator) {
        long now = Instant.now().toEpochMilli();
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO warns (uuid, reason, createdAt, moderator) VALUES (?,?,?,?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, reason);
                stmt.setLong(3, now);
                stmt.setString(4, moderator);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to warn: " + e.getMessage());
            }
        });
    }

    public void removeWarn(UUID uuid, int index) {
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM warns WHERE id = (SELECT id FROM warns WHERE uuid = ? ORDER BY createdAt LIMIT 1 OFFSET ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, index);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to remove warn: " + e.getMessage());
            }
        });
    }

    public String getDefaultBanReason() {
        return configManager.getModeration().getString("moderation.defaultBanReason", "Нарушение правил");
    }

    public String getDefaultMuteReason() {
        return configManager.getModeration().getString("moderation.defaultMuteReason", "Нарушение правил");
    }

    public String getDefaultWarnReason() {
        return configManager.getModeration().getString("moderation.defaultWarnReason", "Предупреждение");
    }

    public boolean isFeatureEnabled() {
        return configManager.isFeatureEnabled("moderation");
    }
}
