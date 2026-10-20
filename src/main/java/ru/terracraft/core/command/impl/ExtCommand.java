package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

public class ExtCommand {

    public static final String PERM_SELF   = "terracraft.core.ext";
    public static final String PERM_OTHERS = "terracraft.core.ext.others";

    private final TerraCorePlugin plugin;

    public ExtCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("ext")
                .withAliases("extinguish")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (player.getFireTicks() <= 0) {
                        plugin.msg().send(player, LangKey.EXT_NOT_BURNING);
                        return;
                    }
                    player.setFireTicks(0);
                    plugin.msg().actionBar(player, LangKey.EXT_SUCCESS);
                    plugin.msg().sound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
                    plugin.msg().logAction(player, "/ext", "player", player.getName());
                    plugin.stats().increment(player, "ext");
                })
                .register();

        new CommandAPICommand("ext")
                .withPermission(PERM_OTHERS)
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    if (target.getFireTicks() <= 0) {
                        plugin.msg().send(sender, LangKey.EXT_NOT_BURNING);
                        return;
                    }

                    target.setFireTicks(0);
                    plugin.msg().send(sender, LangKey.EXT_OTHER, "player", target.getName());
                    plugin.msg().actionBar(target, LangKey.EXT_SUCCESS);
                    plugin.msg().sound(target, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
                    plugin.msg().logAction(sender, "/ext", "player", target.getName());
                    if (sender instanceof Player p) plugin.stats().increment(p, "ext");
                })
                .register();
    }
}