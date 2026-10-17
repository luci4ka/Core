package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /tree <type> — посадить дерево там, куда смотришь.
 *
 * Типы: oak, spruce, birch, jungle, acacia, dark_oak
 *
 * Право: terracraft.core.tree
 */
public class TreeCommand {

    public static final String PERM = "terracraft.core.tree";

    private static final int MAX_DISTANCE = 20;

    private final TerraCorePlugin plugin;

    public TreeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("tree")
                .withPermission(PERM)
                .withArguments(new MultiLiteralArgument("type",
                        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak"))
                .executesPlayer((player, args) -> {
                    String type = (String) args.get("type");
                    TreeType treeType = parseTreeType(type);

                    if (treeType == null) {
                        plugin.msg().send(player, LangKey.TREE_INVALID);
                        return;
                    }

                    Block target = player.getTargetBlockExact(MAX_DISTANCE);
                    if (target == null) {
                        plugin.msg().send(player, LangKey.TREE_INVALID);
                        return;
                    }

                    Block spawnAt = target.getRelative(0, 1, 0);
                    boolean success = player.getWorld()
                            .generateTree(spawnAt.getLocation(), treeType);

                    if (!success) {
                        plugin.msg().send(player, LangKey.TREE_INVALID);
                        return;
                    }

                    plugin.msg().send(player, LangKey.TREE_SUCCESS);
                    plugin.msg().sound(player, Sound.BLOCK_GRASS_PLACE, 0.7f, 1.0f);
                })
                .register();
    }

    private @Nullable TreeType parseTreeType(@NotNull String type) {
        return switch (type) {
            case "oak"      -> TreeType.TREE;
            case "spruce"   -> TreeType.REDWOOD;
            case "birch"    -> TreeType.BIRCH;
            case "jungle"   -> TreeType.JUNGLE;
            case "acacia"   -> TreeType.ACACIA;
            case "dark_oak" -> TreeType.DARK_OAK;
            default         -> null;
        };
    }
}