package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class FlyCommand {

    public static final String PERM_SELF   = "terracraft.core.fly";
    public static final String PERM_OTHERS = "terracraft.core.fly.others";

    private final TerraCorePlugin plugin;

    public FlyCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("fly")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (isWorldDisabled(player)) {
                        plugin.msg().send(player, LangKey.FLY_NOT_ALLOWED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.FLY)) return;

                    boolean nowOn = plugin.fly().toggle(player);
                    plugin.msg().actionBar(player,
                            nowOn ? LangKey.FLY_ENABLED_SELF : LangKey.FLY_DISABLED_SELF);
                    plugin.msg().sound(player,
                            nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                            0.7f, 1.2f);
                    plugin.msg().logAction(player, "/fly",
                            "player", player.getName(),
                            "state", nowOn ? "on" : "off");
                    plugin.stats().increment(player, "fly");
                })
                .register();

        new CommandAPICommand("fly")
                .withPermission(PERM_OTHERS)
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
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    if (isWorldDisabled(target)) {
                        plugin.msg().send(sender, LangKey.FLY_NOT_ALLOWED_WORLD);
                        return;
                    }

                    boolean nowOn = plugin.fly().toggle(target);
                    plugin.msg().send(sender,
                            nowOn ? LangKey.FLY_ENABLED_OTHER : LangKey.FLY_DISABLED_OTHER,
                            "player", target.getName());

                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target,
                                nowOn ? LangKey.FLY_ENABLED_SELF : LangKey.FLY_DISABLED_SELF);
                        plugin.msg().sound(target,
                                nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                                0.7f, 1.2f);
                    }
                    plugin.msg().logAction(sender, "/fly",
                            "player", target.getName(),
                            "state", nowOn ? "on" : "off");
                    if (sender instanceof Player p) plugin.stats().increment(p, "fly");
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().flyDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}