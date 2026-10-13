package ru.terracraft.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Запрет урона от vanished-игроков, если vanishCanHitPlayers = false.
 *
 * Чат, join/quit, hidePlayer — не здесь:
 *   - hidePlayer делает VanishManager.enable()
 *   - чат сам скрывает через player.canSee()
 *   - TAB сам скрывает через metadata "vanished"
 */
public class VanishListener implements Listener {

    private final TerraCorePlugin plugin;

    public VanishListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent event) {
        if (plugin.config().get().vanish().canHitPlayers()) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        if (plugin.vanish().isVanished(damager)) {
            event.setCancelled(true);
        }
    }
}