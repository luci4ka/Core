package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * /burn <player> [seconds] — поджечь игрока.
 *
 * <p>Права:
 * <ul>
 *   <li>{@link #PERM} — поджечь себя;</li>
 *   <li>{@link #PERM_OTHERS} — поджечь другого.</li>
 * </ul>
 *
 * <p>Длительности — в {@code config.yml}, раздел {@code burn}.
 */
public class BurnCommand {

    public static final String PERM = "terracraft.core.burn";
    public static final String PERM_OTHERS = "terracraft.core.burn.others";

    private final TerraCorePlugin plugin;

    public BurnCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        AdminToolsConfig.Burn cfg = plugin.config().get().burn();
        int defaultSeconds = Math.max(1, cfg.defaultSeconds());
        int maxSeconds = Math.max(defaultSeconds, cfg.maxSeconds());

        new CommandAPICommand("burn")
                .withPermission(PERM)
                .withArguments(new StringArgument("target")
                        .replaceSuggestions((info, builder) -> {
                            String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        }))
                .withOptionalArguments(new IntegerArgument("seconds", 1, maxSeconds))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    if (!target.equals(sender)
                            && !sender.hasPermission(PERM_OTHERS)) {
                        plugin.msg().send(sender, LangKey.NO_PERMISSION);
                        return;
                    }

                    int seconds = (int) args.getOrDefault("seconds", defaultSeconds);
                    target.setFireTicks(seconds * 20);

                    if (target.equals(sender)) {
                        plugin.msg().actionBar(target, LangKey.BURN_SUCCESS);
                    } else {
                        plugin.msg().send(sender, LangKey.BURN_OTHER,
                                "player", target.getName());
                        plugin.msg().actionBar(target, LangKey.BURN_SUCCESS);
                    }
                    plugin.msg().sound(target, Sound.ITEM_FLINTANDSTEEL_USE, 0.7f, 1.0f);

                    if (sender instanceof Player p) plugin.stats().increment(p, "burn");

                    plugin.msg().logAction(sender, "/burn",
                            "player", target.getName(),
                            "seconds", seconds);
                })
                .register();
    }
}