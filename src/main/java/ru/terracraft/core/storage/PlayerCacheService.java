package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.SQLManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.sql.ResultSet;
import java.util.UUID;

/**
 * Кэш офлайн-игроков (seen / whois).
 *
 * <p>Записывается при quit, читается для офлайн-игроков.
 * <p>Если MineSQL не загружен — методы тихо возвращают {@code null}
 * (graceful skip в тестах / без плагина).
 */
public class PlayerCacheService {

    private final TerraCorePlugin plugin;

    public PlayerCacheService(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void record(@NotNull Player player) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return;

        Location loc = player.getLocation();
        String ip = player.getAddress() != null
                && player.getAddress().getAddress() != null
                ? player.getAddress().getAddress().getHostAddress()
                : null;

        long now = System.currentTimeMillis();
        long firstJoin = player.getFirstPlayed() > 0 ? player.getFirstPlayed() : now;

        String table = plugin.storage().prefix() + "players";
        String q = "REPLACE INTO " + table + " (uuid, name, first_join, last_seen, last_ip, last_world, last_x, last_y, last_z, last_gamemode) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                sql.executeSQL(q, new Object[]{
                        player.getUniqueId().toString(),
                        player.getName(),
                        firstJoin,
                        now,
                        ip,
                        loc.getWorld() != null ? loc.getWorld().getName() : null,
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        player.getGameMode().name()
                });
            } catch (Exception e) {
                plugin.getLogger().warning("PlayerCache.record: " + e.getMessage());
            }
        });
    }

    public @Nullable PlayerData get(@NotNull UUID uuid) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return null;

        String table = plugin.storage().prefix() + "players";
        try (SQLQuery query = sql.createQuery()
                .inTable(table)
                .addCondition("uuid", uuid.toString())
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            if (!rs.next()) return null;
            return new PlayerData(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("name"),
                    rs.getLong("first_join"),
                    rs.getLong("last_seen"),
                    rs.getString("last_ip"),
                    rs.getString("last_world"),
                    rs.getDouble("last_x"),
                    rs.getDouble("last_y"),
                    rs.getDouble("last_z"),
                    rs.getString("last_gamemode")
            );
        } catch (Exception e) {
            plugin.getLogger().warning("PlayerCache.get: " + e.getMessage());
            return null;
        }
    }

    public @Nullable UUID findUuidByName(@NotNull String name) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return null;

        String table = plugin.storage().prefix() + "players";
        try (SQLQuery query = sql.createQuery()
                .inTable(table)
                .addCondition("name", name)
                .orderBy("last_seen", false)
                .setLimit(1)
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            return rs.next() ? UUID.fromString(rs.getString("uuid")) : null;
        } catch (Exception e) {
            plugin.getLogger().warning("PlayerCache.find: " + e.getMessage());
            return null;
        }
    }

    public record PlayerData(
            @NotNull UUID uuid,
            @NotNull String name,
            long firstJoin,
            long lastSeen,
            @Nullable String lastIp,
            @Nullable String lastWorld,
            double lastX,
            double lastY,
            double lastZ,
            @Nullable String lastGamemode
    ) {}
}