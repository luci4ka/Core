package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class GiveCommand {

    public static final String PERM = "terracraft.core.give";

    private final TerraCorePlugin plugin;

    public GiveCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        AdminToolsConfig.Give cfg = plugin.config().get().give();
        int maxAmount = Math.max(1, cfg.maxAmount());

        new CommandAPICommand("give")
                .withPermission(PERM)
                .withArguments(new StringArgument("target")
                        .replaceSuggestions((info, builder) -> {
                            String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        }))
                .withArguments(new ItemStackArgument("item"))
                .withOptionalArguments(new IntegerArgument("amount", 1, maxAmount))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    ItemStack item = (ItemStack) args.get("item");
                    int amount = (int) args.getOrDefault("amount", 1);

                    giveItems(target, item.getType(), amount);

                    Component itemName = Component.translatable(
                            item.getType().translationKey());

                    plugin.msg().send(sender, LangKey.GIVE_SUCCESS,
                            "player", target.getName(),
                            "value", itemName,
                            "amount", amount);

                    plugin.msg().logAction(sender, "/give",
                            "player", target.getName(),
                            "item", item.getType().getKey().getKey(),
                            "amount", amount);

                    if (!target.equals(sender)) {
                        plugin.msg().send(target, LangKey.GIVE_RECEIVED,
                                "value", itemName,
                                "amount", amount);
                        plugin.msg().sound(target, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    }
                })
                .register();
    }

    private void giveItems(@NotNull Player player,
                           @NotNull Material material,
                           int amount) {
        int max = material.getMaxStackSize();
        while (amount > 0) {
            int stack = Math.min(amount, max);
            ItemStack item = new ItemStack(material, stack);
            var leftover = player.getInventory().addItem(item);
            leftover.values().forEach(rest ->
                    player.getWorld().dropItemNaturally(player.getLocation(), rest));
            amount -= stack;
        }
    }
}