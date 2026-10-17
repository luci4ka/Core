package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /burn <player> [seconds] — поджечь игрока.
 *
 * Право: terracraft.core.burn
 */
public class BurnCommand {

    public static final String PERM = "terracraft.core.burn";

    private static final int DEFAULT_SECONDS = 5;
    private static final int MAX_SECONDS = 60;

    private final TerraCorePlugin plugin;

    public BurnCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("burn")
                .withPermission(PERM)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withOptionalArguments(new IntegerArgument("seconds", 1, MAX_SECONDS))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    int seconds = (int) args.getOrDefault("seconds", DEFAULT_SECONDS);
                    target.setFireTicks(seconds * 20);

                    plugin.msg().send(sender, LangKey.BURN_OTHER,
                            "player", target.getName());
                    plugin.msg().actionBar(target, LangKey.BURN_SUCCESS);
                    plugin.msg().sound(target, Sound.ITEM_FLINTANDSTEEL_USE, 0.7f, 1.0f);
                })
                .register();
    }
}