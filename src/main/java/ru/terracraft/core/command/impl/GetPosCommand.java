package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /getpos [player]
 */
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
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    sendPos(sender, target);
                })
                .register();
    }

    private void sendPos(@NotNull org.bukkit.command.CommandSender sender,
                         @NotNull Player target) {
        Location l = target.getLocation();
        String x = String.format("%.1f", l.getX());
        String y = String.format("%.1f", l.getY());
        String z = String.format("%.1f", l.getZ());

        if (target.equals(sender)) {
            plugin.msg().send(sender, LangKey.GETPOS_SELF, "x", x, "y", y, "z", z);
        } else {
            plugin.msg().send(sender, LangKey.GETPOS_OTHER,
                    "player", target.getName(), "x", x, "y", y, "z", z);
        }
    }
}