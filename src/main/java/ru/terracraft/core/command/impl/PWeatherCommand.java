package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;

public class PWeatherCommand {

    public static final String PERM        = "terracraft.core.pweather";
    public static final String PERM_OTHERS = "terracraft.core.pweather.others";

    private static final String[] PRESETS = {
            "sun", "rain", "storm", "reset"
    };

    private final TerraCorePlugin plugin;

    public PWeatherCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("pweather")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset", PRESETS))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("preset"));
                })
                .register();

        new CommandAPICommand("pweather")
                .withPermission(PERM_OTHERS)
                .withArguments(new MultiLiteralArgument("preset", PRESETS))
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    apply(sender, target, (String) args.get("preset"));
                })
                .register();
    }

    private void apply(@NotNull CommandSender sender,
                       @NotNull Player target, @NotNull String preset) {
        String key = preset.toLowerCase(Locale.ROOT);

        if (key.equals("reset")) {
            target.resetPlayerWeather();
            plugin.msg().send(sender, LangKey.PWEATHER_RESET);
        } else {
            WeatherType type = switch (key) {
                case "sun"  -> WeatherType.CLEAR;
                case "rain", "storm" -> WeatherType.DOWNFALL;
                default -> null;
            };
            if (type == null) {
                plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", preset);
                return;
            }
            target.setPlayerWeather(type);

            LangKey nameKey = presetToNameKey(key);
            String localized = nameKey == null
                    ? key
                    : plugin.lang().raw(target.locale(), nameKey);
            if (localized == null) localized = key;

            plugin.msg().send(sender, LangKey.PWEATHER_SET, "value", localized);
        }
        plugin.msg().sound(target, Sound.ITEM_BUCKET_FILL, 0.5f, 1.5f);
    }

    private @Nullable LangKey presetToNameKey(@NotNull String key) {
        return switch (key) {
            case "sun"   -> LangKey.WEATHER_NAME_SUN;
            case "rain"  -> LangKey.WEATHER_NAME_RAIN;
            case "storm" -> LangKey.WEATHER_NAME_STORM;
            default -> null;
        };
    }
}