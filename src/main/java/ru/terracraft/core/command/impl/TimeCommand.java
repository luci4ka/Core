package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.WorldResolver;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeCommand {

    public static final String PERM        = "terracraft.core.time";
    public static final String PERM_ALL    = "terracraft.core.time.all";
    public static final String PERM_FREEZE = "terracraft.core.time.freeze";
    public static final String PERM_SMOOTH = "terracraft.core.time.smooth";

    private static final int MIN_TICKS = 0;
    private static final int MAX_TICKS = 24000;

    private static final String[] PRESETS = {
            "day", "noon", "night", "midnight", "dawn", "dusk"
    };

    private final TerraCorePlugin plugin;
    private final Map<World, BukkitTask> smoothTasks = new ConcurrentHashMap<>();

    public TimeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /time <preset> [instant] [world|all]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("preset", PRESETS))
                .withOptionalArguments(new LiteralArgument("instant"))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String preset = (String) args.get("preset");
                    Long ticks = presetToTicks(preset);
                    if (ticks == null) {
                        plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", preset);
                        return;
                    }
                    boolean instant = args.get("instant") != null;
                    setTime(sender, ticks, worldArg(args), !instant);
                })
                .register();

        // /time set <preset|ticks> [instant] [world]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new LiteralArgument("set"))
                .withArguments(valueArgument())
                .withOptionalArguments(new LiteralArgument("instant"))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String raw = (String) args.get("value");
                    Long ticks = parseTimeValue(raw);
                    if (ticks == null) {
                        plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", raw);
                        return;
                    }
                    boolean instant = args.get("instant") != null;
                    setTime(sender, ticks, worldArg(args), !instant);
                })
                .register();

        // /time smooth <preset|ticks> [seconds] [world]
        new CommandAPICommand("time")
                .withPermission(PERM_SMOOTH)
                .withArguments(new LiteralArgument("smooth"))
                .withArguments(valueArgument())
                .withOptionalArguments(new IntegerArgument("seconds", 1,
                        plugin.config().get().time().maxSmoothSeconds()))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String raw = (String) args.get("value");
                    Long ticks = parseTimeValue(raw);
                    if (ticks == null) {
                        plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", raw);
                        return;
                    }
                    int defaultSec = plugin.config().get().time().defaultSmoothSeconds();
                    int seconds = (int) args.getOrDefault("seconds", defaultSec);
                    setTime(sender, ticks, worldArg(args), true, seconds);
                })
                .register();

        // /time add <ticks> [world]
        new CommandAPICommand("time")
                .withPermission(PERM)
                .withArguments(new LiteralArgument("add"))
                .withArguments(new IntegerArgument("ticks", -MAX_TICKS, MAX_TICKS))
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    int ticks = (int) args.get("ticks");
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
                    for (World w : worlds) {
                        cancelSmoothTime(w);
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
                .withOptionalArguments(worldArgument())
                .executes((sender, args) -> {
                    String state = (String) args.get("state");
                    String worldArg = worldArg(args);
                    boolean freeze = state.equals("on");

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
                        w.setGameRule(GameRules.ADVANCE_TIME, !freeze);
                    }

                    plugin.msg().send(sender,
                            freeze ? LangKey.TIME_FROZEN : LangKey.TIME_UNFROZEN);
                    plugin.msg().sound(sender, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.0f);
                })
                .register();
    }

    // ============ Аргументы с Tab-комплитом ============

    private @NotNull Argument<String> worldArgument() {
        return new StringArgument("world")
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    List<String> list = new ArrayList<>();
                    list.add("all");
                    String current = info.currentInput().toLowerCase(Locale.ROOT);
                    for (World w : Bukkit.getWorlds()) {
                        if (w.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                            list.add(w.getName());
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    private @NotNull Argument<String> valueArgument() {
        return new StringArgument("value")
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    List<String> list = new ArrayList<>();
                    String current = info.currentInput().toLowerCase(Locale.ROOT);
                    for (String preset : PRESETS) {
                        if (preset.startsWith(current)) list.add(preset);
                    }
                    return list.toArray(new String[0]);
                }));
    }

    private @Nullable String worldArg(@NotNull CommandArguments args) {
        String raw = (String) args.get("world");
        return (raw == null || raw.isBlank()) ? null : raw;
    }

    private boolean isAll(@Nullable String worldArg) {
        return worldArg != null && worldArg.equalsIgnoreCase("all");
    }

    // ============ Парсинг ============

    private @Nullable Long parseTimeValue(@NotNull String raw) {
        String key = raw.toLowerCase(Locale.ROOT);
        Long preset = presetToTicks(key);
        if (preset != null) return preset;
        try {
            long v = Long.parseLong(key);
            if (v < MIN_TICKS || v > MAX_TICKS) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private @Nullable Long presetToTicks(@NotNull String preset) {
        return switch (preset.toLowerCase(Locale.ROOT)) {
            case "day"      -> 1000L;
            case "noon"     -> 6000L;
            case "night"    -> 13000L;
            case "midnight" -> 18000L;
            case "dawn"     -> 23000L;
            case "dusk"     -> 12000L;
            default -> null;
        };
    }

    // ============ Применение ============

    private void setTime(@NotNull CommandSender sender,
                         long ticks,
                         String worldArg,
                         boolean smooth) {
        int defaultSec = plugin.config().get().time().defaultSmoothSeconds();
        setTime(sender, ticks, worldArg, smooth, defaultSec);
    }

    private void setTime(@NotNull CommandSender sender,
                         long ticks,
                         String worldArg,
                         boolean smooth,
                         int seconds) {
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
            if (smooth) {
                startSmoothTime(w, ticks, seconds * 20L);
            } else {
                cancelSmoothTime(w);
                w.setTime(ticks);
            }
        }

        plugin.msg().send(sender, LangKey.TIME_SET,
                "world", worldArg == null ? "текущий" : worldArg,
                "value", ticks);
        plugin.msg().sound(sender, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }

    private void startSmoothTime(@NotNull World world,
                                 long targetTicks,
                                 long durationTicks) {
        cancelSmoothTime(world);

        long startTime = world.getTime();
        long delta = targetTicks - startTime;

        if (delta > 12000)  delta -= 24000;
        if (delta < -12000) delta += 24000;

        long finalDelta = delta;
        long totalSteps = Math.max(1L, durationTicks);

        BukkitTask task = new BukkitRunnable() {
            long step = 0;

            @Override
            public void run() {
                if (step >= totalSteps) {
                    world.setTime(targetTicks);
                    smoothTasks.remove(world);
                    cancel();
                    return;
                }
                step++;
                long current = startTime + (finalDelta * step / totalSteps);
                world.setTime(Math.floorMod(current, 24000L));
            }
        }.runTaskTimer(plugin, 0L, 1L);

        smoothTasks.put(world, task);
    }

    private void cancelSmoothTime(@NotNull World world) {
        BukkitTask task = smoothTasks.remove(world);
        if (task != null) {
            try {
                task.cancel();
            } catch (IllegalStateException ignored) {
                // уже отменена
            }
        }
    }
}