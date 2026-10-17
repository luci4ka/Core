package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

public class CompassCommand {

    public static final String PERM = "terracraft.core.compass";

    private final TerraCorePlugin plugin;

    public CompassCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("compass")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    float yaw = player.getLocation().getYaw();
                    LangKey directionKey = yawToDirectionKey(yaw);

                    // Локализованное название направления — по локали игрока
                    String direction = plugin.lang().raw(player.locale(), directionKey);
                    if (direction == null) direction = directionKey.path();

                    plugin.msg().send(player, LangKey.COMPASS_SELF, "value", direction);
                })
                .register();
    }

    private @NotNull LangKey yawToDirectionKey(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw < 45 || yaw >= 315) return LangKey.DIRECTION_SOUTH;
        if (yaw < 135)              return LangKey.DIRECTION_WEST;
        if (yaw < 225)              return LangKey.DIRECTION_NORTH;
        return LangKey.DIRECTION_EAST;
    }
}