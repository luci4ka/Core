package ru.terracraft.core.command.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Резолвит аргумент «мир» в список миров.
 *
 * null        → мир отправителя (или мир по умолчанию для консоли)
 * "all"       → все миры
 * "название"  → конкретный мир
 * невалидное  → null (сигнал «ошибка»)
 */
public final class WorldResolver {

    private WorldResolver() {}

    public static @Nullable List<World> resolve(
            @NotNull CommandSender sender,
            @Nullable String worldArg) {

        if (worldArg == null) {
            if (sender instanceof Player p) return List.of(p.getWorld());
            List<World> all = Bukkit.getWorlds();
            return all.isEmpty() ? null : List.of(all.get(0));
        }

        if (worldArg.equalsIgnoreCase("all")) {
            List<World> all = Bukkit.getWorlds();
            return all.isEmpty() ? null : all;
        }

        World w = Bukkit.getWorld(worldArg);
        return w == null ? null : List.of(w);
    }
}