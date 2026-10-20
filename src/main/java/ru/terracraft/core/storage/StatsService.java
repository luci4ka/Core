package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLQuery;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.sql.ResultSet;
import java.util.UUID;

/**
 * Статистика использования команд.
 *
 * <p>Инкремент — атомарный {@code INSERT ... ON DUPLICATE KEY UPDATE},
 * что исключает гонку между {@code UPDATE} (0 строк) и {@code INSERT}
 * при одновременных вызовах из разных потоков.
 *
 * <p>Требует MySQL/MariaDB. Для H2 см. альтернативу в комментарии ниже.
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

        // MySQL/MariaDB: атомарный upsert — один statement, без гонки
        String sql = "INSERT INTO " + table + " (uuid, command_key, uses, last_used) "
                + "VALUES (?, ?, 1, ?) "
                + "ON DUPLICATE KEY UPDATE uses = uses + 1, last_used = VALUES(last_used)";

        // H2 (если когда-нибудь понадобится) — раскомментируйте и замените sql:
        // String sql = "MERGE INTO " + table + " (uuid, command_key, uses, last_used) "
        //         + "KEY (uuid, command_key) VALUES (?, ?, 1, ?)";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long now = System.currentTimeMillis();
                plugin.storage().sql().executeSQL(sql,
                        new Object[]{uuid.toString(), commandKey, now});
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