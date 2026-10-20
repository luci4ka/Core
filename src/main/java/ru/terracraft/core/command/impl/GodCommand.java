package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class GodCommand {

    public static final String PERM_SELF   = "terracraft.core.god";
    public static final String PERM_OTHERS = "terracraft.core.god.others";

    private final TerraCorePlugin plugin;

    public GodCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /god — себе
        new CommandAPICommand("god")
                .withAliases("godmode", "invul")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    boolean nowOn = plugin.gods().toggle(player);
                    plugin.msg().actionBar(player,
                            nowOn ? LangKey.GOD_ENABLED_SELF : LangKey.GOD_DISABLED_SELF);
                    plugin.msg().sound(player,
                            nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                            0.7f, 1.0f);
                    plugin.msg().logAction(player, "/god",
                            "player", player.getName(),
                            "state", nowOn ? "on" : "off");
                    plugin.stats().increment(player, "god");
                })
                .register();

        // /god <target>
        new CommandAPICommand("god")
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

                    boolean nowOn = plugin.gods().toggle(target);
                    plugin.msg().send(sender,
                            nowOn ? LangKey.GOD_ENABLED_OTHER : LangKey.GOD_DISABLED_OTHER,
                            "player", target.getName());

                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target,
                                nowOn ? LangKey.GOD_RECEIVED_ENABLED : LangKey.GOD_RECEIVED_DISABLED);
                        plugin.msg().sound(target,
                                nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                                0.7f, 1.0f);
                    }
                    plugin.msg().logAction(sender, "/god",
                            "player", target.getName(),
                            "state", nowOn ? "on" : "off");
                    if (sender instanceof Player p) plugin.stats().increment(p, "god");
                })
                .register();
    }
}