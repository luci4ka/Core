package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

public class ClearCommand {

    public static final String PERM_SELF   = "terracraft.core.clear";
    public static final String PERM_OTHERS = "terracraft.core.clear.others";

    private final TerraCorePlugin plugin;

    public ClearCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("clear")
                .withAliases("clearinventory", "ci")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    player.getInventory().clear();
                    plugin.msg().actionBar(player, LangKey.CLEAR_SUCCESS);
                    plugin.msg().sound(player, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.2f);
                    plugin.msg().logAction(player, "/clear", "target", "self");
                })
                .register();

        new CommandAPICommand("clear")
                .withPermission(PERM_OTHERS)
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    target.getInventory().clear();
                    plugin.msg().actionBar(target, LangKey.CLEAR_SUCCESS);
                    plugin.msg().sound(target, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.2f);

                    if (!target.equals(sender)) {
                        plugin.msg().send(sender, LangKey.CLEAR_SUCCESS);
                    }

                    plugin.msg().logAction(sender, "/clear",
                            "target", target.getName());
                })
                .register();
    }
}