package ru.terracraft.core.task;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Раз в 5 секунд переприменяет видимость vanished-игроков.
 *
 * Это защита от:
 *   - смены прав в LuckPerms без релога
 *   - багов Bukkit при смене мира/респавне
 */
public class VanishTask extends BukkitRunnable {

    private static final long PERIOD_TICKS = 20L * 5; // 5 секунд

    private final TerraCorePlugin plugin;

    public VanishTask(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.vanish().size() == 0) return;
        try {
            plugin.vanish().reapplyAll();
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка VanishTask: " + e.getMessage());
        }
    }

    public void start() {
        runTaskTimer(plugin, PERIOD_TICKS, PERIOD_TICKS);
    }
}