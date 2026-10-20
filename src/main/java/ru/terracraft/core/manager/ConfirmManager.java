package ru.terracraft.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Подтверждения действий: /repair all, /heal (с шансом) и т.п.
 *
 * <p>Игрок должен кликнуть [Да] в течение {@link #TIMEOUT_MS}.
 * Периодическая очистка просроченных записей запускается из {@link #start()}.
 */
public class ConfirmManager {

    private static final long TIMEOUT_MS = 30_000;
    /** Как часто чистить просроченные записи. */
    private static final long CLEANUP_PERIOD_TICKS = 20L * 60;   // раз в минуту

    private final TerraCorePlugin plugin;

    /** UUID → (ключ действия → момент истечения). */
    private final Map<UUID, Map<String, Long>> pending = new ConcurrentHashMap<>();

    /** Задача периодической очистки — чтобы можно было остановить. */
    private BukkitTask cleanupTask;

    public ConfirmManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Запустить периодическую очистку. Вызывать в onEnable.
     * Повторный вызов безопасен — если задача уже активна, ничего не делает.
     */
    public void start() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) return;
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpired,
                CLEANUP_PERIOD_TICKS, CLEANUP_PERIOD_TICKS);
    }

    /** Остановить периодическую очистку. Вызывать в onDisable. */
    public void stop() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }

    /** Запомнить ожидающее подтверждение. */
    public void expect(@NotNull Player player, @NotNull String action) {
        pending.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(action, System.currentTimeMillis() + TIMEOUT_MS);
    }

    /** Проверить, что подтверждение есть и не истекло. */
    public boolean consume(@NotNull Player player, @NotNull String action) {
        Map<String, Long> map = pending.get(player.getUniqueId());
        if (map == null) return false;

        Long until = map.remove(action);
        if (until == null) return false;

        if (map.isEmpty()) pending.remove(player.getUniqueId(), map);

        return until >= System.currentTimeMillis();
    }

    /** Отменить подтверждение. */
    public void cancel(@NotNull Player player, @NotNull String action) {
        Map<String, Long> map = pending.get(player.getUniqueId());
        if (map == null) return;
        map.remove(action);
        if (map.isEmpty()) pending.remove(player.getUniqueId(), map);
    }

    public void clear(@NotNull UUID uuid) {
        pending.remove(uuid);
    }

    // ============ Внутреннее ============

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        pending.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(e -> e.getValue() < now);
            return entry.getValue().isEmpty();
        });
    }
}