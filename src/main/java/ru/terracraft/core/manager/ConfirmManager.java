package ru.terracraft.core.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Подтверждения действий: /repair all, /heal (с шансом) и т.п.
 *
 * Игрок должен кликнуть [Да] в течение 30 секунд.
 */
public class ConfirmManager {

    private static final long TIMEOUT_MS = 30_000;

    /** UUID → (ключ действия → момент истечения). */
    private final Map<UUID, Map<String, Long>> pending = new ConcurrentHashMap<>();

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

        return until >= System.currentTimeMillis();
    }

    /** Отменить подтверждение. */
    public void cancel(@NotNull Player player, @NotNull String action) {
        Map<String, Long> map = pending.get(player.getUniqueId());
        if (map != null) map.remove(action);
    }

    public void clear(@NotNull UUID uuid) {
        pending.remove(uuid);
    }
}