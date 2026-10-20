package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
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

import java.util.ArrayList;
import java.util.List;

/**
 * /itemlore add|set|remove|clear|list
 *
 * <p>Право: terracraft.core.itemlore
 */
public class ItemLoreCommand {

    public static final String PERM = "terracraft.core.itemlore";

    private final TerraCorePlugin plugin;

    public ItemLoreCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("itemlore")
                .withPermission(PERM)
                .withSubcommand(new CommandAPICommand("add")
                        .withArguments(new GreedyStringArgument("line"))
                        .executesPlayer((player, args) -> {
                            ItemStack item = handItem(player);
                            if (item == null) return;

                            ItemMeta meta = item.getItemMeta();
                            if (meta == null) return;

                            List<Component> lore = loreOf(meta);
                            Component line = plugin.msg().mini()
                                    .deserialize((String) args.get("line"));
                            lore.add(line);
                            meta.lore(lore);
                            item.setItemMeta(meta);

                            plugin.msg().send(player, LangKey.ITEMLORE_ADDED);
                            plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
                        }))
                .withSubcommand(new CommandAPICommand("set")
                        .withArguments(new IntegerArgument("line", 1, 100))
                        .withArguments(new GreedyStringArgument("text"))
                        .executesPlayer((player, args) -> {
                            ItemStack item = handItem(player);
                            if (item == null) return;

                            int index = (int) args.get("line") - 1;
                            String raw = (String) args.get("text");

                            ItemMeta meta = item.getItemMeta();
                            if (meta == null) return;

                            List<Component> lore = loreOf(meta);
                            if (index < 0 || index >= lore.size()) {
                                plugin.msg().send(player, LangKey.INVALID_ARGUMENT,
                                        "value", String.valueOf(index + 1));
                                return;
                            }

                            lore.set(index, plugin.msg().mini().deserialize(raw));
                            meta.lore(lore);
                            item.setItemMeta(meta);

                            plugin.msg().send(player, LangKey.ITEMLORE_ADDED);
                            plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
                        }))
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new IntegerArgument("line", 1, 100))
                        .executesPlayer((player, args) -> {
                            ItemStack item = handItem(player);
                            if (item == null) return;

                            int index = (int) args.get("line") - 1;

                            ItemMeta meta = item.getItemMeta();
                            if (meta == null) return;

                            List<Component> lore = loreOf(meta);
                            if (index < 0 || index >= lore.size()) {
                                plugin.msg().send(player, LangKey.INVALID_ARGUMENT,
                                        "value", String.valueOf(index + 1));
                                return;
                            }

                            lore.remove(index);
                            meta.lore(lore.isEmpty() ? null : lore);
                            item.setItemMeta(meta);

                            plugin.msg().send(player, LangKey.ITEMLORE_CLEARED);
                            plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
                        }))
                .withSubcommand(new CommandAPICommand("clear")
                        .executesPlayer((player, args) -> {
                            ItemStack item = handItem(player);
                            if (item == null) return;

                            ItemMeta meta = item.getItemMeta();
                            if (meta == null) return;

                            meta.lore(null);
                            item.setItemMeta(meta);

                            plugin.msg().send(player, LangKey.ITEMLORE_CLEARED);
                            plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
                        }))
                .withSubcommand(new CommandAPICommand("list")
                        .executesPlayer((player, args) -> {
                            ItemStack item = handItem(player);
                            if (item == null) return;

                            ItemMeta meta = item.getItemMeta();
                            if (meta == null) return;

                            List<Component> lore = meta.lore();
                            if (lore == null || lore.isEmpty()) {
                                plugin.msg().send(player, LangKey.ITEMLORE_CLEARED);
                                return;
                            }

                            for (int i = 0; i < lore.size(); i++) {
                                plugin.msg().sendRaw(player, LangKey.ITEMLORE_LINE,
                                        "value", (i + 1) + ". " + renderComponent(lore.get(i)));
                            }
                        }))
                .register();
    }

    // ============ Утилиты ============

    /**
     * Возвращает мутабельную копию lore.
     *
     * <p>{@link ItemMeta#lore()} может вернуть иммутабельный список —
     * {@code new ArrayList<>(...)} решает проблему.
     */
    private @NotNull List<Component> loreOf(@NotNull ItemMeta meta) {
        List<Component> lore = meta.lore();
        return lore == null ? new ArrayList<>() : new ArrayList<>(lore);
    }

    /** Сериализует Component в MiniMessage-строку для отображения. */
    private @NotNull String renderComponent(@NotNull Component c) {
        return plugin.msg().mini().serialize(c);
    }

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