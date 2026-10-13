package ru.terracraft.core.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;

/**
 * Внутренний PlaceholderAPI-расширение TerraCore.
 *
 * Доступные плейсхолдеры:
 *   %terracore_god%            — true/false, в godmode ли игрок
 *   %terracore_fly%            — true/false
 *   %terracore_vanished%       — true/false
 *   %terracore_vanished_count% — сколько vanished онлайн
 *   %terracore_cooldown_<cmd>% — сколько секунд кулдауна осталось (cmd = god, fly, heal, ...)
 *
 * Если PlaceholderAPI нет — этот класс не регистрируется.
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private final TerraCorePlugin plugin;

    public PlaceholderHook(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "terracore";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /** Важно: true, чтобы PAPI не выгружал expansion при reload. */
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String key = params.toLowerCase();

        // ==== Общие (не требуют игрока) ====
        if (key.equals("vanished_count")) {
            return String.valueOf(plugin.vanish().size());
        }

        // ==== Требуется игрок ====
        if (player == null || !player.isOnline()) {
            return null;
        }
        Player online = player.getPlayer();
        if (online == null) return null;

        // ==== Состояния ====
        if (key.equals("god")) {
            return String.valueOf(plugin.gods().isGod(online));
        }
        if (key.equals("fly")) {
            return String.valueOf(plugin.fly().isFlying(online));
        }
        if (key.equals("vanished")) {
            return String.valueOf(plugin.vanish().isVanished(online));
        }

        // ==== Кулдауны: %terracore_cooldown_god% ====
        if (key.startsWith("cooldown_")) {
            String cmd = key.substring("cooldown_".length());
            CommandKey commandKey = parseCommandKey(cmd);
            if (commandKey == null) return null;
            return String.valueOf(plugin.cooldowns().remaining(online, commandKey));
        }

        return null;
    }

    private @Nullable CommandKey parseCommandKey(@NotNull String name) {
        try {
            return CommandKey.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}