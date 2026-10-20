package ru.terracraft.core.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.impl.FlyCommand;
import ru.terracraft.core.command.impl.GodCommand;
import ru.terracraft.core.manager.VanishManager;

import java.util.UUID;

/**
 * Очистка состояния игрока при выходе.
 *
 * <p>Правила сохранения:
 * <ul>
 *   <li>god — сохраняется в БД, если есть {@code terracraft.core.god}
 *       (восстанавливается в {@link PlayerJoinListener}).</li>
 *   <li>fly — сохраняется в БД, если есть {@code terracraft.core.fly}.</li>
 *   <li>gamemode.current — сохраняется в БД всегда
 *       (восстанавливается в {@link PlayerJoinListener}).</li>
 *   <li>vanish — сохраняется в БД (режим «vanish навсегда»),
 *       восстанавливается в {@link PlayerJoinListener#onJoin}.</li>
 *   <li>Кулдауны — сохраняются в БД (не сбрасываются на quit).</li>
 *   <li>Статистика — пишется в БД при использовании команд.</li>
 *   <li>Офлайн-кэш — записывается при quit
 *       ({@link ru.terracraft.core.storage.PlayerCacheService#record}).</li>
 * </ul>
 *
 * <p>Всегда сбрасываются:
 * <ul>
 *   <li>gamemode.previous (для /gm back);</li>
 *   <li>подтверждения (ConfirmManager);</li>
 *   <li>in-memory кэш кулдаунов и vanish (в БД остаётся).</li>
 * </ul>
 */
public class PlayerQuitListener implements Listener {

    private final TerraCorePlugin plugin;

    public PlayerQuitListener(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        // ==== Офлайн-кэш (seen/whois) — пишем в БД ====
        plugin.playerCache().record(player);

        // ==== Fly ====
        if (plugin.fly().isFlying(player)) {
            boolean keepFly = player.hasPermission(FlyCommand.PERM_SELF);
            if (!keepFly) {
                plugin.fly().disable(player);   // снимает и флаг в БД
            } else {
                plugin.fly().clearMemoryOnly(id);   // ← фикс: чистим память
            }
        }

        // ==== God ====
        if (plugin.gods().isGod(player)) {
            if (!player.hasPermission(GodCommand.PERM_SELF)) {
                plugin.gods().clear(id);   // снимает и флаг в БД
            } else {
                plugin.gods().clearMemoryOnly(id);   // ← фикс: чистим память
            }
        }

        // ==== Vanish — снимаем видимость, но НЕ трогаем БД ====
        // Игрок выходит из игры «скрытым», при следующем входе restore() вернёт vanish.
        if (plugin.vanish().isVanished(player)) {
            player.removeMetadata(VanishManager.VANISH_METADATA, plugin);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !online.canSee(player)) {
                    online.showPlayer(plugin, player);
                }
            }
            plugin.vanish().clearMemoryOnly(id);   // БД остаётся
        }

        // ==== Подтверждения — сбрасываем ====
        plugin.confirms().clear(id);

        // ==== Кулдауны — чистим только память, БД остаётся ====
        plugin.cooldowns().clearMemoryOnly(id);

        // ==== Gamemode previous — сбрасываем, current в БД остаётся ====
        plugin.gamemodes().clear(id);
    }
}