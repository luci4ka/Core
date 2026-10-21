package ru.terracraft.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.api.dto.PlayerSnapshot;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.storage.PlayerCacheService;

import java.net.InetSocketAddress;
import java.util.UUID;

public final class TerraCoreAdminAPIImpl implements TerraCoreAdminAPI {

    private final TerraCorePlugin plugin;

    public TerraCoreAdminAPIImpl(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasAccess(@NotNull CommandSender viewer) {
        return viewer.hasPermission(PERM);
    }

    // ============ IP ============

    @Override
    public @Nullable String ip(@NotNull CommandSender viewer, @NotNull UUID target) {
        if (!hasAccess(viewer)) return null;
        Player p = Bukkit.getPlayer(target);
        if (p == null) return null;
        InetSocketAddress addr = p.getAddress();
        if (addr == null || addr.getAddress() == null) return null;
        return addr.getAddress().getHostAddress();
    }

    // ============ Координаты ============

    @Override
    public @Nullable Position position(@NotNull CommandSender viewer, @NotNull UUID target) {
        if (!hasAccess(viewer)) return null;
        Player p = Bukkit.getPlayer(target);
        if (p == null) return null;
        Location l = p.getLocation();
        return new Position(l.getX(), l.getY(), l.getZ(),
                l.getWorld() != null ? l.getWorld().getName() : "?");
    }

    // ============ Время ============

    @Override
    public long firstJoin(@NotNull CommandSender viewer, @NotNull UUID target) {
        if (!hasAccess(viewer)) return 0;
        OfflinePlayer op = Bukkit.getOfflinePlayer(target);
        return op.getFirstPlayed();
    }

    @Override
    public long lastSeen(@NotNull CommandSender viewer, @NotNull UUID target) {
        if (!hasAccess(viewer)) return 0;
        Player p = Bukkit.getPlayer(target);
        if (p != null) return System.currentTimeMillis();   // онлайн — сейчас
        OfflinePlayer op = Bukkit.getOfflinePlayer(target);
        return op.getLastSeen();
    }

    // ============ Кэш ============

    @Override
    public @Nullable PlayerSnapshot cache(@NotNull CommandSender viewer, @NotNull UUID target) {
        if (!hasAccess(viewer)) return null;
        PlayerCacheService.PlayerData d = plugin.playerCache().get(target);
        if (d == null) return null;
        return new PlayerSnapshot(
                d.uuid(), d.name(), d.firstJoin(), d.lastSeen(),
                d.lastIp(), d.lastWorld(),
                d.lastX(), d.lastY(), d.lastZ(), d.lastGamemode()
        );
    }
}