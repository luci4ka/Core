package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class GetPosCommand {

    public static final String PERM        = "terracraft.core.getpos";
    public static final String PERM_OTHERS = "terracraft.core.getpos.others";

    private final TerraCorePlugin plugin;

    public GetPosCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("getpos")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    sendPos(player, player);
                })
                .register();

        new CommandAPICommand("getpos")
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
                    sendPos(sender, target);
                })
                .register();
    }

    private void sendPos(@NotNull org.bukkit.command.CommandSender sender,
                         @NotNull Player target) {
        Location l = target.getLocation();
        String x = String.format(Locale.ROOT, "%.1f", l.getX());
        String y = String.format(Locale.ROOT, "%.1f", l.getY());
        String z = String.format(Locale.ROOT, "%.1f", l.getZ());

        if (target.equals(sender)) {
            plugin.msg().send(sender, LangKey.GETPOS_SELF, "x", x, "y", y, "z", z);
        } else {
            plugin.msg().send(sender, LangKey.GETPOS_OTHER,
                    "player", target.getName(), "x", x, "y", y, "z", z);
        }
    }
}