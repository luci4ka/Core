package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
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
                    plugin.msg().send(player, LangKey.ENDERCHEST_OPENED_SELF);
                })
                .register();

        new CommandAPICommand("enderchest")
                .withAliases("ec", "echest")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executesPlayer((player, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    if (isWorldDisabled(target)) {
                        plugin.msg().send(player, LangKey.ENDERCHEST_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.ENDERCHEST)) return;

                    player.openInventory(target.getEnderChest());
                    plugin.msg().send(player, LangKey.ENDERCHEST_OPENED_OTHER,
                            "player", target.getName());
                    plugin.msg().send(target, LangKey.ENDERCHEST_OPENED_FOR_TARGET,
                            "player", player.getName());
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().workbenchDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}