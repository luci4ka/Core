package ru.terracraft.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Отменяет урон god-игрокам.
 *
 * <p>Очистка состояния при выходе — в {@link PlayerQuitListener}.
 * Восстановление при входе — в {@link PlayerJoinListener}.
 */
public class GodListener implements Listener {

    private final TerraCorePlugin plugin;

    public GodListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.gods().isGod(player)) {
            event.setCancelled(true);
        }
    }
}