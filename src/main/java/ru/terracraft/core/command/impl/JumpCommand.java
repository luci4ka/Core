package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /jump — прыгнуть туда, куда смотришь (до 100 блоков).
 *
 * Право: terracraft.core.jump
 */
public class JumpCommand {

    public static final String PERM = "terracraft.core.jump";

    private static final int MAX_DISTANCE = 100;

    private final TerraCorePlugin plugin;

    public JumpCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("jump")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    Block target = player.getTargetBlockExact(MAX_DISTANCE);
                    if (target == null) {
                        plugin.msg().send(player, LangKey.JUMP_NO_TARGET);
                        return;
                    }

                    Location dest = target.getLocation().add(0.5, 1, 0.5);
                    dest.setYaw(player.getLocation().getYaw());
                    dest.setPitch(player.getLocation().getPitch());

                    player.teleport(dest);
                    plugin.msg().actionBar(player, LangKey.JUMP_SUCCESS);
                    plugin.msg().sound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.5f);
                })
                .register();
    }
}