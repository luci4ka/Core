package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /skull [player] — получить голову игрока (по умолчанию свою).
 *
 * Право: terracraft.core.skull
 */
public class SkullCommand {

    public static final String PERM = "terracraft.core.skull";

    private final TerraCorePlugin plugin;

    public SkullCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("skull")
                .withAliases("head")
                .withPermission(PERM)
                .withOptionalArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executesPlayer((player, args) -> {
                    Player target = (Player) args.getOrDefault("target", player);
                    if (target == null) target = player;

                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    if (meta == null) return;

                    meta.setOwningPlayer(target);
                    meta.displayName(Component.text(target.getName()));
                    skull.setItemMeta(meta);

                    var leftover = player.getInventory().addItem(skull);
                    leftover.values().forEach(rest ->
                            player.getWorld().dropItemNaturally(player.getLocation(), rest));

                    plugin.msg().send(player, LangKey.SKULL_SUCCESS,
                            "player", target.getName());
                    plugin.msg().sound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                })
                .register();
    }
}