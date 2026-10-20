package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.sql.ResultSet;
import java.util.UUID;

/**
 * Хранилище состояний вида (uuid, key, value).
 */
public class StateStore {

    private final TerraCorePlugin plugin;

    public StateStore(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean get(@NotNull UUID uuid, @NotNull String key) {
        String value = getString(uuid, key);
        return value != null && Boolean.parseBoolean(value);
    }

    public void set(@NotNull UUID uuid, @NotNull String key, boolean value) {
        setString(uuid, key, String.valueOf(value));
    }

    public @Nullable String getString(@NotNull UUID uuid, @NotNull String key) {
        String table = plugin.storage().prefix() + "states";
        try (SQLQuery query = plugin.storage().sql().createQuery()
                .inTable(table)
                .addCondition("uuid", uuid.toString())
                .addCondition("state_key", key)
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            return rs.next() ? rs.getString("state_value") : null;
        } catch (Exception e) {
            plugin.getLogger().warning("StateStore.getString: " + e.getMessage());
            return null;
        }
    }

    public void setString(@NotNull UUID uuid, @NotNull String key, @NotNull String value) {
        String table = plugin.storage().prefix() + "states";
        String sql = "REPLACE INTO " + table + " (uuid, state_key, state_value, updated_at) VALUES (?, ?, ?, ?)";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.storage().sql().executeSQL(sql, new Object[]{
                        uuid.toString(), key, value, System.currentTimeMillis()
                });
            } catch (Exception e) {
                plugin.getLogger().warning("StateStore.setString: " + e.getMessage());
            }
        });
    }

    public void clear(@NotNull UUID uuid, @NotNull String key) {
        String table = plugin.storage().prefix() + "states";
        String sql = "DELETE FROM " + table + " WHERE uuid = ? AND state_key = ?";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.storage().sql().executeSQL(sql, new Object[]{uuid.toString(), key});
            } catch (Exception e) {
                plugin.getLogger().warning("StateStore.clear: " + e.getMessage());
            }
        });
    }
}