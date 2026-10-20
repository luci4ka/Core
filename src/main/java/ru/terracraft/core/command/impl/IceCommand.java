package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

public class IceCommand {

    public static final String PERM = "terracraft.core.ice";
    public static final String PERM_OTHERS = "terracraft.core.ice.others";

    private final TerraCorePlugin plugin;

    public IceCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        AdminToolsConfig.Ice cfg = plugin.config().get().ice();
        int defaultSeconds = Math.max(1, cfg.defaultSeconds());
        int maxSeconds = Math.max(defaultSeconds, cfg.maxSeconds());

        new CommandAPICommand("ice")
                .withAliases("freeze")
                .withPermission(PERM)
                .withArguments(Args.playerArg("target"))
                .withOptionalArguments(new IntegerArgument("seconds", 1, maxSeconds))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    if (!target.equals(sender)
                            && !sender.hasPermission(PERM_OTHERS)) {
                        plugin.msg().send(sender, LangKey.NO_PERMISSION);
                        return;
                    }

                    int seconds = (int) args.getOrDefault("seconds", defaultSeconds);
                    target.setFreezeTicks(seconds * 20);

                    if (target.equals(sender)) {
                        plugin.msg().actionBar(target, LangKey.ICE_SUCCESS);
                    } else {
                        plugin.msg().send(sender, LangKey.ICE_OTHER,
                                "player", target.getName());
                        plugin.msg().actionBar(target, LangKey.ICE_SUCCESS);
                    }
                    plugin.msg().sound(target, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                    if (sender instanceof Player p) plugin.stats().increment(p, "ice");

                    plugin.msg().logAction(sender, "/ice",
                            "player", target.getName(),
                            "seconds", seconds);
                })
                .register();
    }
}