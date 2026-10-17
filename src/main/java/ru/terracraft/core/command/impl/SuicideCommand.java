package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /suicide — убить себя.
 *
 * Право: terracraft.core.suicide (default: true)
 */
public class SuicideCommand {

    public static final String PERM = "terracraft.core.suicide";

    private final TerraCorePlugin plugin;

    public SuicideCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("suicide")
                .withAliases("kill", "die")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    plugin.msg().send(player, LangKey.SUICIDE_SUCCESS);
                    player.setHealth(0);
                })
                .register();
    }
}