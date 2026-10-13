package ru.terracraft.core.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Восстанавливает состояния god / fly / gamemode при входе,
 * если у игрока есть право.
 */
public class PlayerJoinListener implements Listener {

    private final TerraCorePlugin plugin;

    public PlayerJoinListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // ==== Fly ====
        if (plugin.fly().isFlying(player)) {
            if (player.hasPermission("terracraft.core.fly")) {
                player.setAllowFlight(true);
                player.setFlying(true);
            } else {
                // Право забрали — чистим, чтобы не залип
                plugin.fly().clear(player.getUniqueId());
            }
        }

        // ==== God — просто оставляем, урон уже отменяется через GodListener ====
        if (plugin.gods().isGod(player) && !player.hasPermission("terracraft.core.god")) {
            plugin.gods().clear(player.getUniqueId());
        }

        // ==== Gamemode ====
        GameMode saved = plugin.gamemodes().getCurrent(player);
        if (saved != null) {
            if (player.hasPermission("terracraft.core.gamemode")) {
                player.setGameMode(saved);
            } else {
                // Право забрали — чистим
                plugin.gamemodes().clearCurrent(player);
            }
        }
    }
}