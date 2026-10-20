package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

public class SpeedCommand {

    public static final String PERM_SELF   = "terracraft.core.speed";
    public static final String PERM_OTHERS = "terracraft.core.speed.others";

    private final TerraCorePlugin plugin;

    public SpeedCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("speed")
                .withPermission(PERM_SELF)
                .withArguments(new IntegerArgument("level", 0, 10))
                .withOptionalArguments(new MultiLiteralArgument("mode", "walk", "fly"))
                .executesPlayer((player, args) -> {
                    int level = (int) args.get("level");
                    String mode = (String) args.getOrDefault("mode", "walk");
                    apply(player, player, level, mode);
                })
                .register();

        new CommandAPICommand("speed")
                .withPermission(PERM_OTHERS)
                .withArguments(new IntegerArgument("level", 0, 10))
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    int level = (int) args.get("level");
                    apply(sender, target, level, "walk");
                })
                .register();

        new CommandAPICommand("speed")
                .withPermission(PERM_OTHERS)
                .withArguments(new IntegerArgument("level", 0, 10))
                .withArguments(new MultiLiteralArgument("mode", "walk", "fly"))
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    int level = (int) args.get("level");
                    String mode = (String) args.get("mode");
                    apply(sender, target, level, mode);
                })
                .register();
    }

    private void apply(@NotNull CommandSender sender,
                       @NotNull Player target,
                       int level,
                       @NotNull String mode) {
        AdminToolsConfig.Speed cfg = plugin.config().get().speed();
        float value = level == 0
                ? (float) (mode.equals("fly") ? cfg.flyDefault() : cfg.walkDefault())
                : level / 10f;

        if (mode.equals("fly")) target.setFlySpeed(value);
        else target.setWalkSpeed(value);

        LangKey key = level == 0
                ? LangKey.SPEED_RESET
                : (mode.equals("fly") ? LangKey.SPEED_SET_FLY : LangKey.SPEED_SET_WALK);

        if (target.equals(sender)) {
            plugin.msg().actionBar(target, key, "value", level);
        } else {
            plugin.msg().send(sender, key,
                    "player", target.getName(), "value", level);
        }

        plugin.msg().sound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
    }
}