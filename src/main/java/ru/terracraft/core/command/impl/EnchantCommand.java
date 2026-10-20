package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnchantCommand {

    public static final String PERM = "terracraft.core.enchant";
    public static final String PERM_UNSAFE = "terracraft.core.enchant.unsafe";

    private final TerraCorePlugin plugin;

    public EnchantCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        AdminToolsConfig.Enchant cfg = plugin.config().get().enchant();
        int maxLevel = Math.max(1, Math.min(255, cfg.maxLevel()));

        // Список ключей энчантов — для Tab-комплита
        List<String> names = new ArrayList<>();
        for (Enchantment e : enchantmentRegistry()) {
            names.add(e.getKey().getKey().toLowerCase(Locale.ROOT));
        }

        new CommandAPICommand("enchant")
                .withAliases("ench")
                .withPermission(PERM)
                .withArguments(Args.registryArg("enchant", names))
                .withOptionalArguments(new IntegerArgument("level", 1, maxLevel))
                .executesPlayer((player, args) -> {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getType().isAir()) {
                        plugin.msg().send(player, LangKey.ENCHANT_EMPTY_HAND);
                        return;
                    }

                    String raw = (String) args.get("enchant");
                    Enchantment ench = findEnchant(raw);
                    if (ench == null) {
                        plugin.msg().send(player, LangKey.ENCHANT_INVALID, "value", raw);
                        return;
                    }

                    int level = (int) args.getOrDefault("level", 1);
                    boolean bypass = player.hasPermission(PERM_UNSAFE);

                    if (!bypass && !ench.canEnchantItem(hand)) {
                        Component itemName = Component.translatable(
                                hand.getType().translationKey());
                        plugin.msg().send(player, LangKey.ENCHANT_INVALID,
                                "value", localizedEnchant(ench),
                                "item", itemName);
                        return;
                    }

                    boolean success = applyEnchant(hand, ench, level, bypass);
                    if (!success) {
                        plugin.msg().send(player, LangKey.ENCHANT_INVALID, "value", raw);
                        return;
                    }

                    plugin.msg().send(player, LangKey.ENCHANT_SUCCESS,
                            "value", localizedEnchant(ench),
                            "amount", level);
                    plugin.msg().sound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.2f);

                    plugin.msg().logAction(player, "/enchant",
                            "ench", ench.getKey().getKey(),
                            "level", level,
                            "bypass", bypass);
                    plugin.stats().increment(player, "enchant");
                })
                .register();
    }

    private @NotNull Component localizedEnchant(@NotNull Enchantment ench) {
        return Component.translatable(
                "enchantment." + ench.getKey().getNamespace() + "." + ench.getKey().getKey());
    }

    private boolean applyEnchant(@NotNull ItemStack hand,
                                 @NotNull Enchantment ench,
                                 int level,
                                 boolean bypass) {
        if (hand.getType() == Material.ENCHANTED_BOOK) {
            if (hand.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                boolean changed = meta.addStoredEnchant(ench, level, true);
                if (changed) hand.setItemMeta(meta);
                return changed;
            }
            return false;
        }

        hand.addUnsafeEnchantment(ench, level);
        return true;
    }

    private @NotNull Registry<Enchantment> enchantmentRegistry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    }

    private @Nullable Enchantment findEnchant(@NotNull String input) {
        String key = input.toLowerCase(Locale.ROOT).replace('-', '_');
        NamespacedKey nk;
        try {
            nk = key.contains(":")
                    ? NamespacedKey.fromString(key)
                    : NamespacedKey.minecraft(key);
        } catch (IllegalArgumentException e) {
            nk = null;
        }
        if (nk != null) {
            Enchantment e = enchantmentRegistry().get(nk);
            if (e != null) return e;
        }
        for (Enchantment e : enchantmentRegistry()) {
            String name = e.getKey().getKey().toLowerCase(Locale.ROOT);
            if (name.equals(key)) return e;
        }
        return null;
    }
}