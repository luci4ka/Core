package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
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
 * /time <day|night|dawn|dusk> [world|all]
 * /time set <ticks> [world]
 * /time add <ticks> [world]
 * /time freeze <on|off> [world]
 */
public class TimeCommand {

    public static final String PERM        = "terracraft.core.time";
    public static final String PERM_ALL    = "terracraft.core.time.all";
    public static final String PERM_FREEZE = "terracraft.core.time.freeze";

    private final TerraCorePlugin plugin;

    public TimeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /time <day|night|dawn|dusk> [world|all]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset", "day", "night", "dawn", "dusk"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    String preset = (String) args.get("preset");
                    String worldArg = (String) args.getOrDefault("world", null);
                    long ticks = switch (preset) {
                        case "day"   -> 1000L;
                        case "night" -> 13000L;
                        case "dawn"  -> 23000L;
                        case "dusk"  -> 12000L;
                        default -> 1000L;
                    };
                    setTime(sender, ticks, worldArg);
                })
                .register();

        // /time set <ticks> [world]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new LiteralArgument("set"))
                .withArguments(new IntegerArgument("ticks", 0, 24000))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    int ticks = (int) args.get("ticks");
                    String worldArg = (String) args.getOrDefault("world", null);
                    setTime(sender, ticks, worldArg);
                })
                .register();

        // /time add <ticks> [world]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new LiteralArgument("add"))
                .withArguments(new IntegerArgument("ticks", -24000, 24000))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    int ticks = (int) args.get("ticks");
                    String worldArg = (String) args.getOrDefault("world", null);
                    for (World w : resolveWorlds(sender, worldArg)) {
                        w.setTime(w.getTime() + ticks);
                    }
                    plugin.msg().send(sender, LangKey.TIME_ADDED, "value", ticks);
                    plugin.msg().sound(sender, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                })
                .register();

        // /time freeze <on|off> [world]
        new CommandAPICommand("time")
                .withPermission(PERM_FREEZE)
                .withArguments(new LiteralArgument("freeze"))
                .withArguments(new MultiLiteralArgument("state", "on", "off"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    String state = (String) args.get("state");
                    String worldArg = (String) args.getOrDefault("world", null);
                    boolean freeze = state.equals("on");

                    for (World w : resolveWorlds(sender, worldArg)) {
                        w.setGameRuleValue("doDaylightCycle", String.valueOf(!freeze));
                    }

                    plugin.msg().send(sender,
                            freeze ? LangKey.TIME_FROZEN : LangKey.TIME_UNFROZEN);
                    plugin.msg().sound(sender, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.0f);
                })
                .register();
    }

    private void setTime(@NotNull org.bukkit.command.CommandSender sender,
                         long ticks, String worldArg) {
        for (World w : resolveWorlds(sender, worldArg)) {
            w.setTime(ticks);
        }
        plugin.msg().send(sender, LangKey.TIME_SET,
                "world", worldArg == null ? "текущий" : worldArg,
                "value", ticks);
        plugin.msg().sound(sender, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }

    private @NotNull java.util.List<World> resolveWorlds(
            @NotNull org.bukkit.command.CommandSender sender, String worldArg) {
        if (worldArg == null) {
            if (sender instanceof Player p) return java.util.List.of(p.getWorld());
            return java.util.List.of(Bukkit.getWorlds().get(0));
        }
        if (worldArg.equalsIgnoreCase("all")) return Bukkit.getWorlds();
        World w = Bukkit.getWorld(worldArg);
        return w == null ? java.util.List.of() : java.util.List.of(w);
    }
}