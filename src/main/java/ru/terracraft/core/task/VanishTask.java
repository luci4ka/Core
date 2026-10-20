package ru.terracraft.core.task;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Переприменяет видимость vanished-игроков с периодом из config.yml.
 *
 * <p>Защита от:
 * <ul>
 *   <li>смены прав в LuckPerms без релога;</li>
 *   <li>багов Bukkit при смене мира/респавне.</li>
 * </ul>
 *
 * <p>Повторный {@link #start()} безопасен — предыдущая задача отменяется.
 */
public class VanishTask extends BukkitRunnable {

    private final TerraCorePlugin plugin;

    /** Стек-трейс печатается только при первой ошибке — чтобы не спамить лог. */
    private volatile boolean firstError = true;

    public VanishTask(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.isEnabled()) return;
        if (plugin.vanish().size() == 0) return;

        try {
            plugin.vanish().reapplyAll();
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка VanishTask: " + e.getMessage());
            if (firstError) {
                firstError = false;
                e.printStackTrace();
            }
        }
    }

    /**
     * Запустить задачу с периодом из config.yml.
     * Если период ≤ 0 — задача не запускается.
     */
    public void start() {
        int period = plugin.config().get().vanish().reapplyPeriodTicks();
        if (period <= 0) {
            plugin.getLogger().info("VanishTask disabled (vanish.reapplyPeriodTicks = 0).");
            return;
        }

        try {
            cancel();
        } catch (IllegalStateException ignored) {
            // Уже отменена
        }
        runTaskTimer(plugin, period, period);
    }

    /** Остановить задачу. */
    public void stop() {
        try {
            cancel();
        } catch (IllegalStateException ignored) {
            // Уже отменена
        }
    }
}