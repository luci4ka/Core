package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

public class EnderchestCommand {

    public static final String PERM_SELF   = "terracraft.core.enderchest";
    public static final String PERM_OTHERS = "terracraft.core.enderchest.others";

    private final TerraCorePlugin plugin;

    public EnderchestCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("enderchest")
                .withAliases("ec", "echest")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (isWorldDisabled(player)) {
                        plugin.msg().send(player, LangKey.ENDERCHEST_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.ENDERCHEST)) return;

                    player.openInventory(player.getEnderChest());
                    plugin.msg().actionBar(player, LangKey.ENDERCHEST_OPENED_SELF);
                    plugin.msg().sound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.0f);
                })
                .register();

        new CommandAPICommand("enderchest")
                .withAliases("ec", "echest")
                .withPermission(PERM_OTHERS)
                .withArguments(Args.playerArg("target"))
                .executesPlayer((player, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(player, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    if (isWorldDisabled(target)) {
                        plugin.msg().send(player, LangKey.ENDERCHEST_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.ENDERCHEST)) return;

                    player.openInventory(target.getEnderChest());

                    plugin.msg().actionBar(player, LangKey.ENDERCHEST_OPENED_OTHER,
                            "player", target.getName());

                    if (!target.equals(player)) {
                        plugin.msg().actionBar(target, LangKey.ENDERCHEST_OPENED_FOR_TARGET,
                                "player", player.getName());
                        plugin.msg().sound(target, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.2f);
                    }

                    plugin.msg().logAction(player, "/enderchest",
                            "target", target.getName());
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().workbenchDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}