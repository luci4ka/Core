package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

public class GiveCommand {

    public static final String PERM = "terracraft.core.give";

    private final TerraCorePlugin plugin;

    public GiveCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("give")
                .withPermission(PERM)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withArguments(new ItemStackArgument("item"))
                .withOptionalArguments(new IntegerArgument("amount", 1, 6400))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    ItemStack item = (ItemStack) args.get("item");
                    int amount = (int) args.getOrDefault("amount", 1);

                    giveItems(target, item.getType(), amount);

                    plugin.msg().send(sender, LangKey.GIVE_SUCCESS,
                            "player", target.getName(),
                            "value", item.getType().name().toLowerCase(),
                            "amount", amount);

                    if (!target.equals(sender)) {
                        plugin.msg().send(target, LangKey.GIVE_RECEIVED,
                                "value", item.getType().name().toLowerCase(),
                                "amount", amount);
                    }
                })
                .register();
    }

    private void giveItems(@NotNull Player player,
                           @NotNull org.bukkit.Material material,
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