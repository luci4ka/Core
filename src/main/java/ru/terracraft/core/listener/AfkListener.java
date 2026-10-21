package ru.terracraft.core.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Обновляет {@code lastActivity} при любом действии игрока.
 *
 * <p>Все вызовы {@link TerraCorePlugin#afk()}.touch() — в главном потоке,
 * чтобы {@link ru.terracraft.api.event.TerraAfkChangeEvent} можно было вызывать безопасно.
 */
public class AfkListener implements Listener {

    private final TerraCorePlugin plugin;

    public AfkListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(@NotNull PlayerJoinEvent e) {
        plugin.afk().touch(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent e) {
        // Только реальное движение (не поворот камеры)
        if (!e.hasChangedPosition()) return;
        // Смена мира — distance() кинет IllegalArgumentException
        if (e.getFrom().getWorld() != e.getTo().getWorld()) return;
        if (e.getFrom().distanceSquared(e.getTo()) > 0.01) {
            plugin.afk().touch(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(@NotNull AsyncChatEvent e) {
        // AsyncChatEvent — async. Переносим touch() в главный поток,
        // чтобы callEvent(TerraAfkChangeEvent) не упал.
        var player = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> plugin.afk().touch(player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
        plugin.afk().touch(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent e) {
        plugin.afk().touch(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent e) {
        plugin.afk().clear(e.getPlayer().getUniqueId());
    }
}