package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /ptime <day|night|dawn|dusk|reset> [player]
 */
public class PTimeCommand {

    public static final String PERM        = "terracraft.core.ptime";
    public static final String PERM_OTHERS = "terracraft.core.ptime.others";

    private final TerraCorePlugin plugin;

    public PTimeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("ptime")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset",
                        "day", "night", "dawn", "dusk", "reset"))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("preset"));
                })
                .register();

        new CommandAPICommand("ptime")
                .withPermission(PERM_OTHERS)
                .withArguments(new MultiLiteralArgument("preset",
                        "day", "night", "dawn", "dusk", "reset"))
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
            target.resetPlayerTime();
            plugin.msg().send(sender, LangKey.PTIME_RESET);
        } else {
            long ticks = switch (preset) {
                case "day"   -> 1000L;
                case "night" -> 13000L;
                case "dawn"  -> 23000L;
                case "dusk"  -> 12000L;
                default -> 1000L;
            };
            target.setPlayerTime(ticks, false);
            plugin.msg().send(sender, LangKey.PTIME_SET, "value", preset);
        }
        plugin.msg().sound(target, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }
}