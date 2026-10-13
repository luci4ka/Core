package ru.terracraft.core.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кулдауны команд.
 *
 * check(player, CommandKey.GOD) → true, если можно; иначе шлёт сообщение и возвращает false.
 * Значение берётся из config.cooldowns().forCommand(key).
 */
public class CooldownManager {

    private final TerraCorePlugin plugin;
    private final Map<UUID, Map<CommandKey, Long>> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean check(@NotNull Player player, @NotNull CommandKey key) {
        int seconds = plugin.config().get().cooldowns().forCommand(key);
        if (seconds <= 0) return true;

        long now = System.currentTimeMillis();
        Map<CommandKey, Long> map = cooldowns.computeIfAbsent(
                player.getUniqueId(),
                k -> new ConcurrentHashMap<>());

        Long until = map.get(key);
        if (until != null && until > now) {
            long left = (until - now) / 1000 + 1;
            plugin.msg().send(player, LangKey.COOLDOWN_ACTIVE, "value", left);
            return false;
        }

        map.put(key, now + seconds * 1000L);
        return true;
    }

    public void clear(@NotNull UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Сколько секунд осталось до конца кулдауна.
     * 0 — если кулдауна нет.
     */
    public int remaining(@NotNull Player player, @NotNull CommandKey key) {
        Map<CommandKey, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;

        Long until = map.get(key);
        if (until == null) return 0;

        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            map.remove(key);
            return 0;
        }
        return (int) (left / 1000) + 1;
    }
}