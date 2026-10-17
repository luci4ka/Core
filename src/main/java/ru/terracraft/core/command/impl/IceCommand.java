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
 * /ice <player> [seconds] — заморозить игрока.
 *
 * Право: terracraft.core.ice
 */
public class IceCommand {

    public static final String PERM = "terracraft.core.ice";

    private static final int DEFAULT_SECONDS = 5;
    private static final int MAX_SECONDS = 60;

    private final TerraCorePlugin plugin;

    public IceCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("ice")
                .withAliases("freeze")
                .withPermission(PERM)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withOptionalArguments(new IntegerArgument("seconds", 1, MAX_SECONDS))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    int seconds = (int) args.getOrDefault("seconds", DEFAULT_SECONDS);
                    target.setFreezeTicks(seconds * 20);

                    plugin.msg().send(sender, LangKey.ICE_OTHER,
                            "player", target.getName());
                    plugin.msg().actionBar(target, LangKey.ICE_SUCCESS);
                    plugin.msg().sound(target, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                })
                .register();
    }
}