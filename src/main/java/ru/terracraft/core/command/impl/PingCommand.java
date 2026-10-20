package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

public class PingCommand {

    public static final String PERM_SELF   = "terracraft.core.ping";
    public static final String PERM_OTHERS = "terracraft.core.ping.others";

    private final TerraCorePlugin plugin;

    public PingCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("ping")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    send(player, player);
                })
                .register();

        new CommandAPICommand("ping")
                .withPermission(PERM_OTHERS)
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    send(sender, target);
                })
                .register();
    }

    private void send(@NotNull CommandSender sender, @NotNull Player target) {
        int ping = target.getPing();
        boolean self = target.equals(sender);

        LangKey key = self ? pickSelfKey(ping) : pickOtherKey(ping);

        if (self) {
            plugin.msg().send(sender, key, "value", ping);
        } else {
            plugin.msg().send(sender, key,
                    "player", target.getName(),
                    "value", ping);
        }
    }

    private @NotNull LangKey pickSelfKey(int ping) {
        AdminToolsConfig.Ping cfg = plugin.config().get().ping();
        if (ping < cfg.goodThreshold()) return LangKey.PING_SELF_GOOD;
        if (ping < cfg.badThreshold())  return LangKey.PING_SELF_MEDIUM;
        return LangKey.PING_SELF_BAD;
    }

    private @NotNull LangKey pickOtherKey(int ping) {
        AdminToolsConfig.Ping cfg = plugin.config().get().ping();
        if (ping < cfg.goodThreshold()) return LangKey.PING_OTHER_GOOD;
        if (ping < cfg.badThreshold())  return LangKey.PING_OTHER_MEDIUM;
        return LangKey.PING_OTHER_BAD;
    }
}