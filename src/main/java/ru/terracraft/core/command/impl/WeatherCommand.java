package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.command.util.WorldResolver;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.List;
import java.util.Locale;

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
                .withOptionalArguments(Args.worldArg("world"))
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
                .withOptionalArguments(Args.worldArg("world"))
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
                .withOptionalArguments(Args.worldArg("world"))
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

    private @Nullable String worldArg(@NotNull CommandArguments args) {
        String raw = (String) args.get("world");
        return (raw == null || raw.isBlank()) ? null : raw;
    }

    private boolean isAll(@Nullable String worldArg) {
        return worldArg != null && worldArg.equalsIgnoreCase("all");
    }

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

        String worldName = worldArg;
        if (worldName == null) {
            Locale locale = (sender instanceof Player p) ? p.locale() : Locale.US;
            String cw = plugin.lang().raw(locale, LangKey.CURRENT_WORLD);
            worldName = cw != null ? cw : "current";
        }

        LangKey key = lock ? LangKey.WEATHER_LOCKED : LangKey.WEATHER_SET;
        plugin.msg().send(sender, key,
                "world", worldName,
                "value", type);
        plugin.msg().sound(sender, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
    }
}