package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

                    Location origin = player.getLocation();
                    List<Player> nearby = new ArrayList<>();

                    for (Player p : player.getWorld().getPlayers()) {
                        if (p.equals(player)) continue;
                        double dist = p.getLocation().distance(origin);
                        if (dist <= radius) nearby.add(p);
                    }

                    if (nearby.isEmpty()) {
                        plugin.msg().send(player, LangKey.NEAR_EMPTY);
                        return;
                    }

                    nearby.sort(Comparator.comparingDouble(
                            p -> p.getLocation().distance(origin)));

                    plugin.msg().sendRaw(player, LangKey.NEAR_HEADER,
                            "value", nearby.size());

                    for (Player p : nearby) {
                        int distance = (int) p.getLocation().distance(origin);
                        plugin.msg().sendRaw(player, LangKey.NEAR_ENTRY,
                                "player", p.getName(),
                                "distance", distance);
                    }
                })
                .register();
    }
}