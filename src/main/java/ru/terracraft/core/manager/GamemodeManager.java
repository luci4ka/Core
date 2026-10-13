package ru.terracraft.core.manager;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит предыдущий GameMode (для /gm back)
 * и текущий (для восстановления между сессиями).
 */
public class GamemodeManager {

    /** Предыдущий режим — для /gm back. */
    private final Map<UUID, GameMode> previous = new ConcurrentHashMap<>();

    /** Текущий режим, установленный через /gm. Восстанавливается при заходе. */
    private final Map<UUID, GameMode> current = new ConcurrentHashMap<>();

    // ============ Previous ============

    public void remember(@NotNull Player player, @NotNull GameMode mode) {
        previous.put(player.getUniqueId(), mode);
    }

    public @Nullable GameMode getPrevious(@NotNull Player player) {
        return previous.get(player.getUniqueId());
    }

    public @Nullable GameMode popPrevious(@NotNull Player player) {
        return previous.remove(player.getUniqueId());
    }

    // ============ Current ============

    /** Запомнить, что игрок был в этом режиме (для восстановления). */
    public void setCurrent(@NotNull Player player, @NotNull GameMode mode) {
        current.put(player.getUniqueId(), mode);
    }

    public @Nullable GameMode getCurrent(@NotNull Player player) {
        return current.get(player.getUniqueId());
    }

    /** Убрать запись — если игрок сменил режим вручную, а не через /gm. */
    public void clearCurrent(@NotNull Player player) {
        current.remove(player.getUniqueId());
    }

    // ============ Общее ============

    public void clear(@NotNull UUID uuid) {
        previous.remove(uuid);
        // current НЕ удаляем — оно восстанавливается при заходе
    }
}