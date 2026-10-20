package ru.terracraft.core.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.impl.FlyCommand;
import ru.terracraft.core.command.impl.GamemodeCommand;
import ru.terracraft.core.command.impl.GodCommand;
import ru.terracraft.core.command.impl.VanishCommand;

/**
 * Восстанавливает состояния god / fly / gamemode / vanish при входе.
 *
 * <p>Сохранение при выходе — в {@link PlayerQuitListener}.
 */
public class PlayerJoinListener implements Listener {

    private final TerraCorePlugin plugin;

    public PlayerJoinListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.playerCache().record(player);

        // ==== God ====
        plugin.gods().restore(player);
        if (plugin.gods().isGod(player) && !player.hasPermission(GodCommand.PERM_SELF)) {
            plugin.gods().clear(player.getUniqueId());
        }

        // ==== Fly ====
        plugin.fly().restore(player);
        if (plugin.fly().isFlying(player)) {
            if (player.hasPermission(FlyCommand.PERM_SELF)) {
                player.setAllowFlight(true);
                player.setFlying(true);
            } else {
                plugin.fly().clear(player.getUniqueId());
            }
        }

        // ==== Gamemode — восстановить из БД, затем применить ====
        plugin.gamemodes().restore(player);
        GameMode saved = plugin.gamemodes().getCurrent(player);
        if (saved != null) {
            if (player.hasPermission(GamemodeCommand.PERM_SELF)) {
                player.setGameMode(saved);
            } else {
                plugin.gamemodes().clearCurrent(player);
            }
        }

        // ==== Vanish — восстановить из БД + скрыть других vanished от новичка ====
        plugin.vanish().restore(player);
        if (plugin.vanish().isVanished(player)
                && !player.hasPermission(VanishCommand.PERM_SELF)) {
            plugin.vanish().clear(player.getUniqueId());
        }
        plugin.vanish().applyVisibilityForViewer(player);
    }
}