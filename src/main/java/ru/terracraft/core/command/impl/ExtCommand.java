package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /ext [player] — потушить игрока (по умолчанию себя).
 *
 * Права:
 *   terracraft.core.ext         — себе
 *   terracraft.core.ext.others  — другим
 */
public class ExtCommand {

    public static final String PERM_SELF   = "terracraft.core.ext";
    public static final String PERM_OTHERS = "terracraft.core.ext.others";

    private final TerraCorePlugin plugin;

    public ExtCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /ext — себе
        new CommandAPICommand("ext")
                .withAliases("extinguish")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (player.getFireTicks() <= 0) {
                        plugin.msg().send(player, LangKey.EXT_NOT_BURNING);
                        return;
                    }
                    player.setFireTicks(0);
                    plugin.msg().actionBar(player, LangKey.EXT_SUCCESS);
                    plugin.msg().sound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
                })
                .register();

        // /ext <target>
        new CommandAPICommand("ext")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    if (target.getFireTicks() <= 0) {
                        plugin.msg().send(sender, LangKey.EXT_NOT_BURNING);
                        return;
                    }

                    target.setFireTicks(0);
                    plugin.msg().send(sender, LangKey.EXT_OTHER, "player", target.getName());
                    plugin.msg().actionBar(target, LangKey.EXT_SUCCESS);
                    plugin.msg().sound(target, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
                })
                .register();
    }
}