package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /top — телепорт на самый верхний блок над игроком.
 *
 * Право: terracraft.core.top
 */
public class TopCommand {

    public static final String PERM = "terracraft.core.top";

    private final TerraCorePlugin plugin;

    public TopCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("top")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    Location loc = player.getLocation();
                    Location top = player.getWorld()
                            .getHighestBlockAt(loc)
                            .getLocation()
                            .add(0.5, 1, 0.5);

                    top.setYaw(loc.getYaw());
                    top.setPitch(loc.getPitch());

                    if (top.getBlockY() <= loc.getBlockY()) {
                        plugin.msg().send(player, LangKey.TOP_NO_BLOCK);
                        return;
                    }

                    player.teleport(top);

                    // Чат — «телепорт на Y=120» полезно видеть дольше 2 секунд
                    plugin.msg().send(player, LangKey.TOP_SUCCESS,
                            "y", top.getBlockY());
                    plugin.msg().sound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.5f);
                })
                .register();
    }
}