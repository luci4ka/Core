package ru.terracraft.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.api.event.TerraAfkChangeEvent;
import ru.terracraft.core.TerraCorePlugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AFK-статус игроков.
 *
 * <p>Не хранится в БД — AFK сбрасывается при выходе (это сессионное состояние).
 *
 * <p>Детект:
 * <ul>
 *   <li>Автоматически — по таймауту без движения/чата (см. {@code config.afk})</li>
 *   <li>Вручную — {@code /afk} или {@link #set(Player, boolean)}</li>
 * </ul>
 */
public class AfkManager {

    private final TerraCorePlugin plugin;

    /** UUID → AFK? */
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    /** UUID → последняя активность (ms). */
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    public AfkManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    // ============ API ============

    public boolean isAfk(@NotNull UUID uuid) {
        return afk.contains(uuid);
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(afk);
    }

    /**
     * Установить AFK-статус. Генерирует {@link TerraAfkChangeEvent}, если
     * состояние изменилось.
     */
    public void set(@NotNull Player player, boolean value) {
        UUID id = player.getUniqueId();
        boolean changed = value ? afk.add(id) : afk.remove(id);
        if (!changed) return;

        // Возврат из AFK — сбрасываем таймер активности
        if (!value) {
            lastActivity.put(id, System.currentTimeMillis());
        }

        Bukkit.getPluginManager().callEvent(new TerraAfkChangeEvent(player, value));
    }

    public void toggle(@NotNull Player player) {
        set(player, !isAfk(player.getUniqueId()));
    }

    /** Отметить активность (сбрасывает AFK + таймер). */
    public void touch(@NotNull Player player) {
        UUID id = player.getUniqueId();
        lastActivity.put(id, System.currentTimeMillis());
        if (afk.remove(id)) {
            Bukkit.getPluginManager().callEvent(new TerraAfkChangeEvent(player, false));
        }
    }

    public long lastActivity(@NotNull UUID uuid) {
        return lastActivity.getOrDefault(uuid, 0L);
    }

    // ============ Очистка ============

    /** Полная очистка (при выходе). */
    public void clear(@NotNull UUID uuid) {
        afk.remove(uuid);
        lastActivity.remove(uuid);
    }
}