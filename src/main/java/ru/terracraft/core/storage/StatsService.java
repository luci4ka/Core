package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLQuery;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.sql.ResultSet;
import java.util.UUID;

/**
 * Статистика использования команд.
 */
public class StatsService {

    private final TerraCorePlugin plugin;

    public StatsService(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void increment(@NotNull Player player, @NotNull String commandKey) {
        increment(player.getUniqueId(), commandKey);
    }

    public void increment(@NotNull UUID uuid, @NotNull String commandKey) {
        String table = plugin.storage().prefix() + "stats";
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long now = System.currentTimeMillis();
                // 1. UPDATE
                String updateSql = "UPDATE " + table + " SET uses = uses + 1, last_used = ? WHERE uuid = ? AND command_key = ?";
                Integer updated = plugin.storage().sql().executeSQL(updateSql,
                        new Object[]{now, uuid.toString(), commandKey});

                // 2. INSERT, если не было строки
                if (updated == null || updated == 0) {
                    String insertSql = "INSERT INTO " + table + " (uuid, command_key, uses, last_used) VALUES (?, ?, 1, ?)";
                    plugin.storage().sql().executeSQL(insertSql,
                            new Object[]{uuid.toString(), commandKey, now});
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Stats.increment: " + e.getMessage());
            }
        });
    }

    public long getUses(@NotNull UUID uuid, @NotNull String commandKey) {
        String table = plugin.storage().prefix() + "stats";
        try (SQLQuery query = plugin.storage().sql().createQuery()
                .inTable(table)
                .addCondition("uuid", uuid.toString())
                .addCondition("command_key", commandKey)
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            return rs.next() ? rs.getLong("uses") : 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Stats.get: " + e.getMessage());
            return 0;
        }
    }
}