package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /itemname <имя>     — установить имя предмету в руке (MiniMessage)
 * /itemname reset     — сбросить имя
 *
 * <p>Право: terracraft.core.itemname
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
                .withSubcommand(new CommandAPICommand("reset")
                        .executesPlayer((PlayerCommandExecutor) (player, args) -> reset(player)))
                .withArguments(new GreedyStringArgument("name"))
                .executesPlayer((player, args) -> {
                    String raw = (String) args.get("name");
                    if (raw == null || raw.isBlank()) {
                        reset(player);
                        return;
                    }
                    rename(player, raw);
                })
                .register();
    }

    // ============ Действия ============

    private void reset(@NotNull Player player) {
        ItemStack item = handItem(player);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.msg().send(player, LangKey.ITEM_INVALID_HAND);
            return;
        }

        meta.displayName(null);
        item.setItemMeta(meta);

        plugin.msg().send(player, LangKey.ITEMNAME_REMOVED);
        plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
    }

    private void rename(@NotNull Player player, @NotNull String raw) {
        ItemStack item = handItem(player);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.msg().send(player, LangKey.ITEM_INVALID_HAND);
            return;
        }

        Component name = plugin.msg().mini().deserialize(raw);
        meta.displayName(name);
        item.setItemMeta(meta);

        plugin.msg().send(player, LangKey.ITEMNAME_SUCCESS);
        plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
    }

    // ============ Утилиты ============

    /** Возвращает предмет в руке или null (и шлёт сообщение). */
    private @Nullable ItemStack handItem(@NotNull Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            plugin.msg().send(player, LangKey.ITEM_INVALID_HAND);
            return null;
        }
        return item;
    }
}