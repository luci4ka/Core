package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Comparator;
import java.util.List;

/**
 * /near [radius]
 *
 * Показывает игроков в радиусе (по умолчанию 200 блоков).
 *
 * Право: terracraft.core.near
 */
public class NearCommand {

    public static final String PERM = "terracraft.core.near";

    private static final int DEFAULT_RADIUS = 200;

    private final TerraCorePlugin plugin;

    public NearCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("near")
                .withPermission(PERM)
                .withOptionalArguments(new IntegerArgument("radius", 1, 500))
                .executesPlayer((player, args) -> {
                    int radius = (int) args.getOrDefault("radius", DEFAULT_RADIUS);

                    List<Player> nearby = player.getWorld().getPlayers().stream()
                            .filter(p -> !p.equals(player))
                            .filter(p -> p.getLocation().distance(player.getLocation()) <= radius)
                            .sorted(Comparator.comparingDouble(
                                    p -> p.getLocation().distance(player.getLocation())))
                            .toList();

                    if (nearby.isEmpty()) {
                        plugin.msg().send(player, LangKey.NEAR_EMPTY);
                        return;
                    }

                    plugin.msg().sendRaw(player, LangKey.NEAR_HEADER,
                            "value", nearby.size());

                    for (Player p : nearby) {
                        int distance = (int) p.getLocation().distance(player.getLocation());
                        plugin.msg().sendRaw(player, LangKey.NEAR_ENTRY,
                                "player", p.getName(),
                                "distance", distance);
                    }
                })
                .register();
    }
}