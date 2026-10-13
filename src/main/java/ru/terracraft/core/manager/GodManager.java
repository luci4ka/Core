package ru.terracraft.core.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит UUID игроков в godmode.
 *
 * Никакой логики Bukkit — только состояние.
 * Очистка на PlayerQuitEvent — в GodListener.
 */
public class GodManager {

    private final Set<UUID> gods = ConcurrentHashMap.newKeySet();

    public boolean isGod(@NotNull Player player) {
        return gods.contains(player.getUniqueId());
    }

    public boolean isGod(@NotNull UUID uuid) {
        return gods.contains(uuid);
    }

    /** @return true, если режим включён (игрока не было в списке). */
    public boolean enable(@NotNull Player player) {
        return gods.add(player.getUniqueId());
    }

    /** @return true, если режим выключен (игрок был в списке). */
    public boolean disable(@NotNull Player player) {
        return gods.remove(player.getUniqueId());
    }

    /** @return true, если режим переключён (был выключен → включён). */
    public boolean toggle(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (gods.contains(id)) {
            gods.remove(id);
            return false;
        }
        gods.add(id);
        return true;
    }

    public void clear(@NotNull UUID uuid) {
        gods.remove(uuid);
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(gods);
    }

    public int size() {
        return gods.size();
    }
}