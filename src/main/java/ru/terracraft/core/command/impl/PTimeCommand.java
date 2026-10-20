package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;

public class PTimeCommand {

    public static final String PERM        = "terracraft.core.ptime";
    public static final String PERM_OTHERS = "terracraft.core.ptime.others";

    private static final String[] PRESETS = {
            "day", "noon", "night", "midnight", "dawn", "dusk", "reset"
    };

    private final TerraCorePlugin plugin;

    public PTimeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("ptime")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset", PRESETS))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("preset"));
                })
                .register();

        new CommandAPICommand("ptime")
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
            target.resetPlayerTime();
            plugin.msg().send(sender, LangKey.PTIME_RESET);
        } else {
            Long ticks = presetToTicks(key);
            if (ticks == null) {
                plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", preset);
                return;
            }
            target.setPlayerTime(ticks, false);

            LangKey nameKey = presetToNameKey(key);
            String localized = nameKey == null
                    ? key
                    : plugin.lang().raw(target.locale(), nameKey);
            if (localized == null) localized = key;

            plugin.msg().send(sender, LangKey.PTIME_SET,
                    "value", localized,
                    "ticks", ticks);
        }
        plugin.msg().sound(target, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }

    private @Nullable Long presetToTicks(@NotNull String key) {
        return switch (key) {
            case "day"      -> 1000L;
            case "noon"     -> 6000L;
            case "night"    -> 13000L;
            case "midnight" -> 18000L;
            case "dawn"     -> 23000L;
            case "dusk"     -> 12000L;
            default -> null;
        };
    }

    private @Nullable LangKey presetToNameKey(@NotNull String key) {
        return switch (key) {
            case "day"      -> LangKey.TIME_NAME_DAY;
            case "noon"     -> LangKey.TIME_NAME_NOON;
            case "night"    -> LangKey.TIME_NAME_NIGHT;
            case "midnight" -> LangKey.TIME_NAME_MIDNIGHT;
            case "dawn"     -> LangKey.TIME_NAME_DAWN;
            case "dusk"     -> LangKey.TIME_NAME_DUSK;
            default -> null;
        };
    }
}