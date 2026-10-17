package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /itemname <имя>
 *
 * Устанавливает кастомное имя предмету в руке.
 * Поддерживает MiniMessage-теги (<red>, <gradient> и т.д.).
 *
 * /itemname reset — сбросить имя.
 *
 * Право: terracraft.core.itemname
 */
public class ItemNameCommand {

    public static final String PERM = "terracraft.core.itemname";

    private final TerraCorePlugin plugin;

    public ItemNameCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("itemname")
                .withAliases("rename", "itemrename")
                .withPermission(PERM)
                .withArguments(new GreedyStringArgument("name"))
                .executesPlayer((player, args) -> {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.AIR) {
                        plugin.msg().send(player, LangKey.ITEM_INVALID_HAND);
                        return;
                    }

                    String raw = (String) args.get("name");
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) {
                        plugin.msg().send(player, LangKey.ITEM_INVALID_HAND);
                        return;
                    }

                    // reset — сбросить имя
                    if (raw.equalsIgnoreCase("reset") || raw.isBlank()) {
                        meta.displayName(null);
                        item.setItemMeta(meta);
                        plugin.msg().send(player, LangKey.ITEMNAME_REMOVED);
                        plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
                        return;
                    }

                    Component name = plugin.msg().mini().deserialize(raw);
                    meta.displayName(name);
                    item.setItemMeta(meta);

                    plugin.msg().send(player, LangKey.ITEMNAME_SUCCESS);
                    plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
                })
                .register();
    }
}