package ru.terracraft.core.manager;

import cc.carm.lib.easysql.api.SQLQuery;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кулдауны команд.
 */
public class CooldownManager {

    private final TerraCorePlugin plugin;

    private final Map<UUID, Map<CommandKey, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CommandKey, Long>> pending = new ConcurrentHashMap<>();

    public CooldownManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean check(@NotNull Player player, @NotNull CommandKey key) {
        if (!tryAcquire(player, key)) return false;
        commit(player, key);
        return true;
    }

    public boolean tryAcquire(@NotNull Player player, @NotNull CommandKey key) {
        int seconds = plugin.config().get().cooldowns().forCommand(key);
        if (seconds <= 0) return true;

        long now = System.currentTimeMillis();
        Map<CommandKey, Long> map = cooldowns.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());

        Long until = map.get(key);
        if (until == null) {
            until = loadFromDb(player.getUniqueId(), key);
            if (until != null) map.put(key, until);
        }

        if (until != null && until > now) {
            long left = (until - now) / 1000 + 1;
            plugin.msg().send(player, LangKey.COOLDOWN_ACTIVE, "value", left);
            return false;
        }

        pending.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(key, now + seconds * 1000L);
        return true;
    }

    public void commit(@NotNull Player player, @NotNull CommandKey key) {
        Map<CommandKey, Long> map = pending.get(player.getUniqueId());
        if (map == null) return;
        Long until = map.remove(key);
        if (until == null) return;

        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(key, until);

        saveAsync(player.getUniqueId(), key, until);
    }

    public void rollback(@NotNull Player player, @NotNull CommandKey key) {
        Map<CommandKey, Long> map = pending.get(player.getUniqueId());
        if (map != null) map.remove(key);
    }

    public void clear(@NotNull UUID uuid) {
        cooldowns.remove(uuid);
        pending.remove(uuid);
        deleteAsync(uuid);
    }

    public int remaining(@NotNull Player player, @NotNull CommandKey key) {
        Map<CommandKey, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) {
            Long db = loadFromDb(player.getUniqueId(), key);
            if (db == null) return 0;
            long left = db - System.currentTimeMillis();
            return left <= 0 ? 0 : (int) (left / 1000) + 1;
        }

        Long until = map.get(key);
        if (until == null) return 0;
        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            map.remove(key);
            return 0;
        }
        return (int) (left / 1000) + 1;
    }

    private @Nullable Long loadFromDb(@NotNull UUID uuid, @NotNull CommandKey key) {
        String table = plugin.storage().prefix() + "cooldowns";
        try (SQLQuery query = plugin.storage().sql().createQuery()
                .inTable(table)
                .addCondition("uuid", uuid.toString())
                .addCondition("command_key", key.name())
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            return rs.next() ? rs.getLong("expires_at") : null;
        } catch (Exception e) {
            plugin.getLogger().warning("loadCooldown: " + e.getMessage());
            return null;
        }
    }

    private void saveAsync(@NotNull UUID uuid, @NotNull CommandKey key, long until) {
        String table = plugin.storage().prefix() + "cooldowns";
        String sql = "REPLACE INTO " + table + " (uuid, command_key, expires_at) VALUES (?, ?, ?)";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.storage().sql().executeSQL(sql, new Object[]{uuid.toString(), key.name(), until});
            } catch (Exception e) {
                plugin.getLogger().warning("saveCooldown: " + e.getMessage());
            }
        });
    }

    private void deleteAsync(@NotNull UUID uuid) {
        String table = plugin.storage().prefix() + "cooldowns";
        String sql = "DELETE FROM " + table + " WHERE uuid = ?";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.storage().sql().executeSQL(sql, new Object[]{uuid.toString()});
            } catch (Exception e) {
                plugin.getLogger().warning("deleteCooldown: " + e.getMessage());
            }
        });
    }

    public void cleanupExpired() {
        String table = plugin.storage().prefix() + "cooldowns";
        String sql = "DELETE FROM " + table + " WHERE expires_at < ?";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Integer deleted = plugin.storage().sql().executeSQL(sql, new Object[]{System.currentTimeMillis()});
                if (deleted != null && deleted > 0) {
                    plugin.getLogger().info("Cooldown cleanup: removed " + deleted);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("cleanupCooldown: " + e.getMessage());
            }
        });
    }

    public void clearMemoryOnly(@NotNull UUID uuid) {
        cooldowns.remove(uuid);
        pending.remove(uuid);
    }
}