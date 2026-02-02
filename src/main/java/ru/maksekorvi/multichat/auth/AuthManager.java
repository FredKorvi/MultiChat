package ru.maksekorvi.multichat.auth;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;
import org.bukkit.plugin.java.JavaPlugin;
import ru.maksekorvi.multichat.config.ConfigManager;
import ru.maksekorvi.multichat.database.Database;
import ru.maksekorvi.multichat.util.MessageService;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final Database database;
    private final ConfigManager configManager;
    private final MessageService messages;
    private final Map<UUID, AuthState> authStates = new ConcurrentHashMap<>();
    private final Set<UUID> loading = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;

    public AuthManager(JavaPlugin plugin, Database database, ConfigManager configManager, MessageService messages) {
        this.plugin = plugin;
        this.database = database;
        this.configManager = configManager;
        this.messages = messages;
    }

    public void loadOnlineSessions() {
        plugin.getServer().getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void loadPlayer(Player player) {
        loading.add(player.getUniqueId());
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                boolean registered = rs.next();
                boolean sessionValid = false;
                if (registered) {
                    String lastIp = rs.getString("lastIp");
                    long sessionUntil = rs.getLong("sessionUntil");
                    String ip = getIp(player);
                    sessionValid = ip != null && ip.equalsIgnoreCase(lastIp) && sessionUntil > Instant.now().toEpochMilli();
                }
                boolean finalRegistered = registered;
                boolean finalSessionValid = sessionValid;
                database.runSync(() -> {
                    authStates.put(player.getUniqueId(), new AuthState(finalRegistered, finalSessionValid));
                    loading.remove(player.getUniqueId());
                    if (finalRegistered && !finalSessionValid) {
                        messages.send(player, "errors.auth-required");
                    }
                    if (!finalRegistered) {
                        messages.sendRaw(player, messages.format("errors.not-registered"));
                    }
                });
            } catch (SQLException e) {
                database.runSync(() -> messages.sendRaw(player, "&cОшибка базы данных."));
            }
        });
    }

    public void unregister(UUID uuid) {
        authStates.remove(uuid);
        loading.remove(uuid);
    }

    public boolean isLoading(UUID uuid) {
        return loading.contains(uuid);
    }

    public boolean isAuthenticated(Player player) {
        AuthState state = authStates.get(player.getUniqueId());
        return state != null && state.isLoggedIn();
    }

    public boolean isRegistered(Player player) {
        AuthState state = authStates.get(player.getUniqueId());
        return state != null && state.isRegistered();
    }

    public void register(Player player, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        long now = Instant.now().toEpochMilli();
        String ip = getIp(player);
        long sessionUntil = now + getSessionMillis();
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO users (uuid, name, passhash, regDate, lastIp, lastLogin, sessionUntil) VALUES (?,?,?,?,?,?,?)")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, hash);
                stmt.setLong(4, now);
                stmt.setString(5, ip);
                stmt.setLong(6, now);
                stmt.setLong(7, sessionUntil);
                stmt.executeUpdate();
                database.runSync(() -> {
                    authStates.put(player.getUniqueId(), new AuthState(true, true));
                    messages.send(player, "info.register-success");
                });
            } catch (SQLException e) {
                database.runSync(() -> messages.sendRaw(player, "&cОшибка базы данных."));
            }
        });
    }

    public void login(Player player, String password) {
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT passhash FROM users WHERE uuid = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    database.runSync(() -> messages.send(player, "errors.not-registered"));
                    return;
                }
                String hash = rs.getString("passhash");
                if (!BCrypt.checkpw(password, hash)) {
                    database.runSync(() -> messages.send(player, "errors.wrong-password"));
                    return;
                }
                long now = Instant.now().toEpochMilli();
                long sessionUntil = now + getSessionMillis();
                String ip = getIp(player);
                try (PreparedStatement update = connection.prepareStatement("UPDATE users SET lastIp = ?, lastLogin = ?, sessionUntil = ? WHERE uuid = ?")) {
                    update.setString(1, ip);
                    update.setLong(2, now);
                    update.setLong(3, sessionUntil);
                    update.setString(4, player.getUniqueId().toString());
                    update.executeUpdate();
                }
                database.runSync(() -> {
                    authStates.put(player.getUniqueId(), new AuthState(true, true));
                    messages.send(player, "info.login-success");
                });
            } catch (SQLException e) {
                database.runSync(() -> messages.sendRaw(player, "&cОшибка базы данных."));
            }
        });
    }

    public void changePassword(Player player, String oldPassword, String newPassword) {
        database.runAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT passhash FROM users WHERE uuid = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    database.runSync(() -> messages.send(player, "errors.not-registered"));
                    return;
                }
                String hash = rs.getString("passhash");
                if (!BCrypt.checkpw(oldPassword, hash)) {
                    database.runSync(() -> messages.send(player, "errors.wrong-password"));
                    return;
                }
                String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                try (PreparedStatement update = connection.prepareStatement("UPDATE users SET passhash = ? WHERE uuid = ?")) {
                    update.setString(1, newHash);
                    update.setString(2, player.getUniqueId().toString());
                    update.executeUpdate();
                }
                database.runSync(() -> messages.send(player, "info.changepass-success"));
            } catch (SQLException e) {
                database.runSync(() -> messages.sendRaw(player, "&cОшибка базы данных."));
            }
        });
    }

    public void logout(Player player) {
        AuthState state = authStates.get(player.getUniqueId());
        if (state != null) {
            authStates.put(player.getUniqueId(), new AuthState(state.isRegistered(), false));
        }
    }

    public CommandExecutor getRegisterCommand() {
        return new RegisterCommand(this, messages);
    }

    public CommandExecutor getLoginCommand() {
        return new LoginCommand(this, messages);
    }

    public CommandExecutor getChangePasswordCommand() {
        return new ChangePasswordCommand(this, messages);
    }

    public CommandExecutor getLogoutCommand() {
        return new LogoutCommand(this, messages);
    }

    public long getSessionMillis() {
        long minutes = configManager.getAuth().getLong("auth.sessionMinutes", 60L);
        return minutes * 60_000L;
    }

    public java.util.List<String> getCommandWhitelist() {
        return configManager.getConfig().getStringList("settings.auth.commandWhitelist");
    }

    private String getIp(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }
}
