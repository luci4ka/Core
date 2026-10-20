package ru.terracraft.core.manager;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.storage.StateStore;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlyManager {

    private static final String KEY = "fly";

    private final Set<UUID> flying = ConcurrentHashMap.newKeySet();
    private final StateStore store;

    public FlyManager(@NotNull StateStore store) {
        this.store = store;
    }

    public boolean isFlying(@NotNull Player player) {
        return flying.contains(player.getUniqueId());
    }

    public boolean enable(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (!flying.add(id)) return false;
        player.setAllowFlight(true);
        player.setFlying(true);
        store.set(id, KEY, true);
        return true;
    }

    public boolean disable(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (!flying.remove(id)) return false;
        if (player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
        store.clear(id, KEY);
        return true;
    }

    /**
     * Убрать из памяти, НЕ трогая БД.
     *
     * <p>Используется в quit-сценарии: fly сохраняется в БД,
     * при следующем входе {@link #restore(Player)} вернёт его.
     */
    public void clearMemoryOnly(@NotNull UUID uuid) {
        flying.remove(uuid);
    }

    public boolean toggle(@NotNull Player player) {
        return isFlying(player) ? !disable(player) : enable(player);
    }

    public void clear(@NotNull UUID uuid) {
        flying.remove(uuid);
        store.clear(uuid, KEY);
    }

    /** Восстанавливает состояние из БД при входе. */
    public void restore(@NotNull Player player) {
        if (store.get(player.getUniqueId(), KEY)) {
            flying.add(player.getUniqueId());
        }
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(flying);
    }

    public int size() {
        return flying.size();
    }
}