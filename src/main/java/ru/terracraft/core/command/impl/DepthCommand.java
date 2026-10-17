package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /depth — на какой высоте над бедроком находится игрок.
 *
 * Право: terracraft.core.depth
 */
public class DepthCommand {

    public static final String PERM = "terracraft.core.depth";

    private final TerraCorePlugin plugin;

    public DepthCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("depth")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    int blockY = player.getLocation().getBlockY();
                    int minY = player.getWorld().getMinHeight();
                    int depth = blockY - minY;

                    plugin.msg().send(player, LangKey.DEPTH_SELF, "value", depth);
                })
                .register();
    }
}