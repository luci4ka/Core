package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.WorldResolver;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /weather <sun|rain|storm> [world|all]
 * /weather lock <sun|rain|storm> [world]
 * /weather unlock [world]
 *
 * <p>Права:
 * <ul>
 *   <li>{@link #PERM} — базовое;</li>
 *   <li>{@link #PERM_ALL} — менять погоду во всех мирах ({@code all});</li>
 *   <li>{@link #PERM_LOCK} — lock / unlock.</li>
 * </ul>
 *
 * <p>Длительности после {@code unlock} — в {@code config.yml}, раздел {@code weather}.
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
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String type = (String) args.get("type");
                    String worldArg = worldArg(args);
                    apply(sender, type, worldArg, false);
                })
                .register();

        // /weather lock <type> [world]
        new CommandAPICommand("weather")
                .withPermission(PERM_LOCK)
                .withArguments(new LiteralArgument("lock"))
                .withArguments(new MultiLiteralArgument("type", "sun", "rain", "storm"))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String type = (String) args.get("type");
                    String worldArg = worldArg(args);
                    apply(sender, type, worldArg, true);
                })
                .register();

        // /weather unlock [world]
        new CommandAPICommand("weather")
                .withPermission(PERM_LOCK)
                .withArguments(new LiteralArgument("unlock"))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String worldArg = worldArg(args);

                    if (isAll(worldArg) && !sender.hasPermission(PERM_ALL)) {
                        plugin.msg().send(sender, LangKey.NO_PERMISSION);
                        return;
                    }

                    List<World> worlds = WorldResolver.resolve(sender, worldArg);
                    if (worlds == null) {
                        plugin.msg().send(sender, LangKey.INVALID_ARGUMENT,
                                "value", worldArg == null ? "world" : worldArg);
                        return;
                    }

                    AdminToolsConfig.Weather cfg = plugin.config().get().weather();
                    for (World w : worlds) {
                        w.setStorm(false);
                        w.setThundering(false);
                        w.setClearWeatherDuration(cfg.clearDuration());
                        w.setWeatherDuration(cfg.rainDuration());
                        w.setThunderDuration(cfg.thunderDuration());
                    }
                    plugin.msg().send(sender, LangKey.WEATHER_UNLOCKED);
                })
                .register();
    }

    // ============ Аргумент world ============

    private @NotNull Argument<String> worldArgument() {
        return new StringArgument("world")
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    List<String> list = new ArrayList<>();
                    list.add("all");

                    String current = info.currentInput().toLowerCase(Locale.ROOT);
                    for (World w : Bukkit.getWorlds()) {
                        String name = w.getName();
                        if (name.toLowerCase(Locale.ROOT).startsWith(current)) {
                            list.add(name);
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    private @Nullable String worldArg(@NotNull dev.jorel.commandapi.executors.CommandArguments args) {
        String raw = (String) args.get("world");
        return (raw == null || raw.isBlank()) ? null : raw;
    }

    private boolean isAll(@Nullable String worldArg) {
        return worldArg != null && worldArg.equalsIgnoreCase("all");
    }

    // ============ Применение ============

    private void apply(@NotNull CommandSender sender,
                       @NotNull String type,
                       String worldArg,
                       boolean lock) {
        if (isAll(worldArg) && !sender.hasPermission(PERM_ALL)) {
            plugin.msg().send(sender, LangKey.NO_PERMISSION);
            return;
        }

        List<World> worlds = WorldResolver.resolve(sender, worldArg);
        if (worlds == null) {
            plugin.msg().send(sender, LangKey.INVALID_ARGUMENT,
                    "value", worldArg == null ? "world" : worldArg);
            return;
        }

        for (World w : worlds) {
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
            if (lock) {
                w.setWeatherDuration(Integer.MAX_VALUE);
                w.setThunderDuration(Integer.MAX_VALUE);
            }
        }

        LangKey key = lock ? LangKey.WEATHER_LOCKED : LangKey.WEATHER_SET;
        plugin.msg().send(sender, key,
                "world", worldArg == null ? "текущий" : worldArg,
                "value", type);
        plugin.msg().sound(sender, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
    }
}