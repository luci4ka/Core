package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /weather <sun|rain|storm> [world|all]
 * /weather lock <sun|rain|storm> [world]
 * /weather unlock [world]
 */
public class WeatherCommand {

    public static final String PERM      = "terracraft.core.weather";
    public static final String PERM_ALL  = "terracraft.core.weather.all";
    public static final String PERM_LOCK = "terracraft.core.weather.lock";

    private final TerraCorePlugin plugin;

    public WeatherCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /weather <type> [world|all]
        new CommandAPICommand("weather")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("type", "sun", "rain", "storm"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    String type = (String) args.get("type");
                    String worldArg = (String) args.getOrDefault("world", null);
                    apply(sender, type, worldArg, false);
                })
                .register();

        // /weather lock <type> [world]
        new CommandAPICommand("weather")
                .withPermission(PERM_LOCK)
                .withArguments(new LiteralArgument("lock"))
                .withArguments(new MultiLiteralArgument("type", "sun", "rain", "storm"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    String type = (String) args.get("type");
                    String worldArg = (String) args.getOrDefault("world", null);
                    apply(sender, type, worldArg, true);
                })
                .register();

        // /weather unlock [world]
        new CommandAPICommand("weather")
                .withPermission(PERM_LOCK)
                .withArguments(new LiteralArgument("unlock"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    String worldArg = (String) args.getOrDefault("world", null);
                    for (World w : resolveWorlds(sender, worldArg)) {
                        w.setStorm(false);
                        w.setThundering(false);
                        w.setWeatherDuration(Integer.MAX_VALUE);
                    }
                    plugin.msg().send(sender, LangKey.WEATHER_UNLOCKED);
                })
                .register();
    }

    private void apply(@NotNull org.bukkit.command.CommandSender sender,
                       @NotNull String type,
                       String worldArg,
                       boolean lock) {
        for (World w : resolveWorlds(sender, worldArg)) {
            switch (type) {
                case "sun" -> {
                    w.setStorm(false);
                    w.setThundering(false);
                }
                case "rain" -> {
                    w.setStorm(true);
                    w.setThundering(false);
                }
                case "storm" -> {
                    w.setStorm(true);
                    w.setThundering(true);
                }
            }
            if (lock) w.setWeatherDuration(Integer.MAX_VALUE);
        }

        LangKey key = lock ? LangKey.WEATHER_LOCKED : LangKey.WEATHER_SET;
        plugin.msg().send(sender, key,
                "world", worldArg == null ? "текущий" : worldArg,
                "value", type);
        plugin.msg().sound(sender, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
    }

    private @NotNull java.util.List<World> resolveWorlds(
            @NotNull org.bukkit.command.CommandSender sender, String worldArg) {
        if (worldArg == null) {
            if (sender instanceof Player p) return java.util.List.of(p.getWorld());
            return java.util.List.of(Bukkit.getWorlds().get(0));
        }
        if (worldArg.equalsIgnoreCase("all")) {
            return Bukkit.getWorlds();
        }
        World w = Bukkit.getWorld(worldArg);
        return w == null ? java.util.List.of() : java.util.List.of(w);
    }
}