package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /ping [player]
 *
 * Без аргумента — свой пинг.
 * С аргументом — пинг другого игрока.
 *
 * Право: terracraft.core.ping
 */
public class PingCommand {

    public static final String PERM_SELF   = "terracraft.core.ping";
    public static final String PERM_OTHERS = "terracraft.core.ping.others";

    private final TerraCorePlugin plugin;

    public PingCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /ping — себе
        new CommandAPICommand("ping")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    plugin.msg().send(player, LangKey.PING_SELF,
                            "value", player.getPing());
                })
                .register();

        // /ping <target>
        new CommandAPICommand("ping")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", "?");
                        return;
                    }
                    send(sender, target);
                })
                .register();
    }

    private void send(@NotNull CommandSender sender, @NotNull Player target) {
        if (target.equals(sender)) {
            plugin.msg().send(sender, LangKey.PING_SELF, "value", target.getPing());
        } else {
            plugin.msg().send(sender, LangKey.PING_OTHER,
                    "player", target.getName(),
                    "value", target.getPing());
        }
    }
}