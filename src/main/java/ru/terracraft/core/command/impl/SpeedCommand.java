package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

public class SpeedCommand {

    public static final String PERM_SELF   = "terracraft.core.speed";
    public static final String PERM_OTHERS = "terracraft.core.speed.others";

    private static final float WALK_DEFAULT = 0.1f;
    private static final float FLY_DEFAULT  = 0.1f;

    private final TerraCorePlugin plugin;

    public SpeedCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /speed <0-10> [walk|fly] — себе
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

        // /speed <0-10> [walk|fly] <target> — другим
        new CommandAPICommand("speed")
                .withPermission(PERM_OTHERS)
                .withArguments(new IntegerArgument("level", 0, 10))
                .withOptionalArguments(
                        new MultiLiteralArgument("mode", "walk", "fly")
                                .combineWith(new EntitySelectorArgument.OnePlayer("target"))
                )
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    int level = (int) args.get("level");
                    String mode = (String) args.getOrDefault("mode", "walk");
                    apply(sender, target, level, mode);
                })
                .register();
    }

    private void apply(@NotNull org.bukkit.command.CommandSender sender,
                       @NotNull Player target,
                       int level,
                       @NotNull String mode) {
        float value = level == 0
                ? (mode.equals("fly") ? FLY_DEFAULT : WALK_DEFAULT)
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