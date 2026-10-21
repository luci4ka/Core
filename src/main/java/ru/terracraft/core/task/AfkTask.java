package ru.terracraft.core.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Раз в 20 тиков проверяет, кто "завис" без движения/чата и помечает AFK.
 */
public class AfkTask extends BukkitRunnable {

    private final TerraCorePlugin plugin;

    public AfkTask(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.isEnabled()) return;

        long timeoutMs = plugin.config().get().afk().timeoutSeconds() * 1000L;
        if (timeoutMs <= 0) return;

        long now = System.currentTimeMillis();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.afk().isAfk(p.getUniqueId())) continue;
            if (p.hasPermission("terracraft.core.afk.bypass")) continue;

            long last = plugin.afk().lastActivity(p.getUniqueId());
            if (last <= 0) {
                plugin.afk().touch(p);   // первый раз — инициализируем
                continue;
            }

            if (now - last >= timeoutMs) {
                plugin.afk().set(p, true);
            }
        }
    }
}