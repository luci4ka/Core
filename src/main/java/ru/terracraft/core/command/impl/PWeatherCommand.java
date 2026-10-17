package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /pweather <sun|rain|reset> [player]
 */
public class PWeatherCommand {

    public static final String PERM        = "terracraft.core.pweather";
    public static final String PERM_OTHERS = "terracraft.core.pweather.others";

    private final TerraCorePlugin plugin;

    public PWeatherCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("pweather")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset", "sun", "rain", "reset"))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("preset"));
                })
                .register();

        new CommandAPICommand("pweather")
                .withPermission(PERM_OTHERS)
                .withArguments(new MultiLiteralArgument("preset", "sun", "rain", "reset"))
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    apply(sender, target, (String) args.get("preset"));
                })
                .register();
    }

    private void apply(@NotNull org.bukkit.command.CommandSender sender,
                       @NotNull Player target, @NotNull String preset) {
        if (preset.equals("reset")) {
            target.resetPlayerWeather();
            plugin.msg().send(sender, LangKey.PWEATHER_RESET);
        } else {
            target.setPlayerWeather(preset.equals("sun")
                    ? WeatherType.CLEAR
                    : WeatherType.DOWNFALL);
            plugin.msg().send(sender, LangKey.PWEATHER_SET, "value", preset);
        }
        plugin.msg().sound(target, Sound.ITEM_BUCKET_FILL, 0.5f, 1.5f);
    }
}