package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

public class WorkbenchCommand {

    public static final String PERM = "terracraft.core.workbench";

    private final TerraCorePlugin plugin;

    public WorkbenchCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("workbench")
                .withAliases("wb")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    if (isWorldDisabled(player)) {
                        plugin.msg().send(player, LangKey.WORKBENCH_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.WORKBENCH)) return;

                    player.openInventory(
                            MenuType.CRAFTING.builder()
                                    .title(Component.text("Верстак"))
                                    .checkReachable(false)
                                    .build(player)
                    );
                    plugin.msg().send(player, LangKey.WORKBENCH_OPENED);
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().workbenchDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}