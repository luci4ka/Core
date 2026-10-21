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

    private final Map<UUID, GameMode> previous = new ConcurrentHashMap<>();
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

    public void setCurrent(@NotNull Player player, @NotNull GameMode mode) {
        current.put(player.getUniqueId(), mode);
        store.setString(player.getUniqueId(), KEY, mode.name());
    }

    public @Nullable GameMode getCurrent(@NotNull Player player) {
        return current.get(player.getUniqueId());
    }

    /**
     * Читает сохранённый GameMode из БД (работает и для офлайн-игроков).
     * Для онлайн — сначала смотрит в память, потом в БД.
     */
    public @Nullable String getSavedGamemode(@NotNull UUID uuid) {
        GameMode mem = current.get(uuid);
        if (mem != null) return mem.name();
        return store.getString(uuid, KEY);
    }

    public void clearCurrent(@NotNull Player player) {
        current.remove(player.getUniqueId());
        store.clear(player.getUniqueId(), KEY);
    }

    // ============ Восстановление ============

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

    public void clear(@NotNull UUID uuid) {
        previous.remove(uuid);
        current.remove(uuid);
    }
}