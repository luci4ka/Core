package ru.terracraft.core.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кто сейчас в /fly.
 *
 * Нужен отдельно от Player#isFlying(), потому что:
 *   1. isFlying() может быть true из-за элитр/spectator.
 *   2. На quit нужно решить, снимать ли allowFlight.
 */
public class FlyManager {

    private final Set<UUID> flying = ConcurrentHashMap.newKeySet();

    public boolean isFlying(@NotNull Player player) {
        return flying.contains(player.getUniqueId());
    }

    /** @return true, если полёт включён. */
    public boolean enable(@NotNull Player player) {
        if (!flying.add(player.getUniqueId())) return false;
        player.setAllowFlight(true);
        player.setFlying(true);
        return true;
    }

    /** @return true, если полёт выключен. */
    public boolean disable(@NotNull Player player) {
        if (!flying.remove(player.getUniqueId())) return false;
        // Не снимаем allowFlight, если игрок в креативе/спектаторе — там он всегда true.
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE
                && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
        return true;
    }

    /** @return true, если полёт включился (был выключен). */
    public boolean toggle(@NotNull Player player) {
        return isFlying(player) ? !disable(player) : enable(player);
    }

    public void clear(@NotNull UUID uuid) {
        flying.remove(uuid);
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(flying);
    }
}