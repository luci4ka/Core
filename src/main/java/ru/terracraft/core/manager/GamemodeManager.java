package ru.terracraft.core.manager;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.storage.StateStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит предыдущий GameMode (для /gm back)
 * и текущий (для восстановления между сессиями).
 *
 * <p>{@code current} сохраняется в БД через {@link StateStore}
 * (state_key = {@value #KEY}). {@code previous} — только в памяти.
 */
public class GamemodeManager {

    /** Ключ в таблице states. */
    private static final String KEY = "gamemode";

    /** Предыдущий режим — для /gm back. Только в памяти. */
    private final Map<UUID, GameMode> previous = new ConcurrentHashMap<>();

    /** Текущий режим, установленный через /gm. Сохраняется в БД. */
    private final Map<UUID, GameMode> current = new ConcurrentHashMap<>();

    private final StateStore store;

    public GamemodeManager(@NotNull StateStore store) {
        this.store = store;
    }

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
        store.setString(player.getUniqueId(), KEY, mode.name());
    }

    public @Nullable GameMode getCurrent(@NotNull Player player) {
        return current.get(player.getUniqueId());
    }

    /** Убрать запись — если игрок сменил режим вручную, а не через /gm. */
    public void clearCurrent(@NotNull Player player) {
        current.remove(player.getUniqueId());
        store.clear(player.getUniqueId(), KEY);
    }

    // ============ Восстановление ============

    /**
     * Восстанавливает {@code current} из БД при входе игрока.
     * Значение кладётся в память (в {@code current}).
     */
    public void restore(@NotNull Player player) {
        String saved = store.getString(player.getUniqueId(), KEY);
        if (saved == null) return;

        try {
            GameMode mode = GameMode.valueOf(saved);
            current.put(player.getUniqueId(), mode);
        } catch (IllegalArgumentException ignored) {
            store.clear(player.getUniqueId(), KEY);
        }
    }

    // ============ Общее ============

    /**
     * Очистка при выходе игрока.
     *
     * <p>{@code previous} удаляем полностью — он нужен только в рамках сессии.
     * {@code current} тоже удаляем из памяти: значение остаётся в БД
     * (его вернёт {@link #restore(Player)} при следующем входе),
     * а память не растёт на каждого уникального игрока.
     */
    public void clear(@NotNull UUID uuid) {
        previous.remove(uuid);
        current.remove(uuid);   // ← фикс утечки памяти
    }
}