package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.Comparator;
import java.util.List;

/**
 * /near [radius]
 *
 * <p>Радиус по умолчанию — в {@code config.yml}, раздел {@code near}.
 * Максимум — 500 блоков.
 */
public class NearCommand {

    public static final String PERM = "terracraft.core.near";

    private static final int MAX_RADIUS = 500;

    private final TerraCorePlugin plugin;

    public NearCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        AdminToolsConfig.Near cfg = plugin.config().get().near();
        int defaultRadius = Math.max(1, Math.min(cfg.defaultRadius(), MAX_RADIUS));

        new CommandAPICommand("near")
                .withPermission(PERM)
                .withOptionalArguments(new IntegerArgument("radius", 1, MAX_RADIUS))
                .executesPlayer((player, args) -> {
                    int radius = (int) args.getOrDefault("radius", defaultRadius);

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