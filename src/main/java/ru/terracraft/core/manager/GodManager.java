package ru.terracraft.core.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.storage.StateStore;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GodManager {

    private static final String KEY = "god";

    private final Set<UUID> gods = ConcurrentHashMap.newKeySet();
    private final StateStore store;

    public GodManager(@NotNull StateStore store) {
        this.store = store;
    }

    public boolean isGod(@NotNull Player player) {
        return isGod(player.getUniqueId());
    }

    public boolean isGod(@NotNull UUID uuid) {
        return gods.contains(uuid);
    }

    public boolean enable(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (!gods.add(id)) return false;
        store.set(id, KEY, true);
        return true;
    }

    public boolean disable(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (!gods.remove(id)) return false;
        store.clear(id, KEY);
        return true;
    }

    public boolean toggle(@NotNull Player player) {
        return isGod(player) ? !disable(player) : enable(player);
    }

    public void clear(@NotNull UUID uuid) {
        gods.remove(uuid);
        store.clear(uuid, KEY);
    }

    /**
     * Убрать из памяти, НЕ трогая БД.
     *
     * <p>Используется в quit-сценарии: god сохраняется в БД,
     * при следующем входе {@link #restore(Player)} вернёт его.
     */
    public void clearMemoryOnly(@NotNull UUID uuid) {
        gods.remove(uuid);
    }

    public void restore(@NotNull Player player) {
        if (store.get(player.getUniqueId(), KEY)) {
            gods.add(player.getUniqueId());
        }
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(gods);
    }

    public int size() {
        return gods.size();
    }
}