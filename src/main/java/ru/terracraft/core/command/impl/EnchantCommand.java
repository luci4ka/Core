package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * /enchant <энчант> [уровень]
 * <p>
 * Накладывает энчант на предмет в руке.
 * addUnsafeEnchantment — игнорирует ванильные ограничения (уровень выше максимума).
 * <p>
 * Право: terracraft.core.enchant
 */
public class EnchantCommand {

    public static final String PERM = "terracraft.core.enchant";

    private static final int MAX_LEVEL = 255;

    private final TerraCorePlugin plugin;

    public EnchantCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("enchant")
                .withAliases("ench")
                .withPermission(PERM)
                .withArguments(new StringArgument("enchant")
                        .replaceSuggestions((info, builder) -> {
                            String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                            int count = 0;
                            for (Enchantment e : enchantmentRegistry()) {
                                String name = e.getKey().getKey().toLowerCase(Locale.ROOT);
                                if (name.startsWith(prefix)) {
                                    builder.suggest(name);
                                    if (++count >= 50) break;
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        }))
                .withOptionalArguments(new IntegerArgument("level", 1, MAX_LEVEL))
                .executesPlayer((player, args) -> {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getType().isAir()) {
                        plugin.msg().send(player, LangKey.ENCHANT_INVALID, "value", "пустая рука");
                        return;
                    }

                    String raw = (String) args.get("enchant");
                    Enchantment ench = findEnchant(raw);
                    if (ench == null) {
                        plugin.msg().send(player, LangKey.ENCHANT_INVALID, "value", raw);
                        return;
                    }

                    int level = (int) args.getOrDefault("level", 1);

                    hand.addUnsafeEnchantment(ench, level);

                    plugin.msg().send(player, LangKey.ENCHANT_SUCCESS,
                            "value", ench.getKey().getKey(),
                            "amount", level);
                })
                .register();
    }

    // ============ Реестр энчантов ============

    /**
     * Возвращает реестр энчантов через новый API Paper.
     * Registry.ENCHANTMENT deprecated с 1.21 — используем RegistryAccess.
     */
    private @NotNull Registry<Enchantment> enchantmentRegistry() {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT);
    }

    /**
     * Ищет энчант по имени.
     * Поддерживает: "sharpness", "minecraft:sharpness".
     */
    private @Nullable Enchantment findEnchant(@NotNull String input) {
        String key = input.toLowerCase(Locale.ROOT);

        // 1. По ключу реестра
        NamespacedKey nk = key.contains(":")
                ? NamespacedKey.fromString(key)
                : NamespacedKey.minecraft(key);
        if (nk != null) {
            Enchantment e = enchantmentRegistry().get(nk);
            if (e != null) return e;
        }

        // 2. Перебором по имени ключа
        for (Enchantment e : enchantmentRegistry()) {
            String name = e.getKey().getKey().toLowerCase(Locale.ROOT);
            if (name.equals(key)) return e;
        }
        return null;
    }
}