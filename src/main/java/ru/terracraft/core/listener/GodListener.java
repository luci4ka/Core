package ru.terracraft.core.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.manager.VanishManager;

import java.util.UUID;

/**
 * Отменяет урон god-игрокам и чистит состояние при выходе.
 *
 * Состояния god / fly / gamemode НЕ снимаются, если у игрока есть право —
 * они восстанавливаются в PlayerJoinListener.
 */
public class GodListener implements Listener {

    private final TerraCorePlugin plugin;

    public GodListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.gods().isGod(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        boolean keepGod = player.hasPermission("terracraft.core.god");
        boolean keepFly = player.hasPermission("terracraft.core.fly");

        // ==== Fly ====
        // Снимаем allowFlight только если права нет
        if (plugin.fly().isFlying(player) && !keepFly) {
            if (player.getGameMode() != GameMode.CREATIVE
                    && player.getGameMode() != GameMode.SPECTATOR) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
            plugin.fly().clear(id);
        }
        // Если право есть — FlyManager сохраняет UUID, состояние восстановится

        // ==== God ====
        if (!keepGod) {
            plugin.gods().clear(id);
        }
        // Если право есть — GodManager сохраняет UUID, урон будет отменяться при заходе

        // ==== Vanish — всегда снимаем ====
        if (plugin.vanish().isVanished(player)) {
            player.removeMetadata(VanishManager.VANISH_METADATA, plugin);
        }
        plugin.vanish().clear(id);
        plugin.confirms().clear(id);   // ← добавить
        // ==== Cooldowns — сбрасываем ====
        plugin.cooldowns().clear(id);

        // ==== Gamemode previous — сбрасываем (для /gm back) ====
        // current сохраняется — восстановится при заходе
        plugin.gamemodes().clear(id);
    }
}