package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

public class HatCommand {

    public static final String PERM = "terracraft.core.hat";

    private final TerraCorePlugin plugin;

    public HatCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("hat")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.HAT)) return;

                    ItemStack hand = player.getInventory().getItemInMainHand();
                    boolean emptyHandAllowed = plugin.config().get().hat().allowEmptyHand();

                    if (hand.getType() == Material.AIR) {
                        if (!emptyHandAllowed) {
                            plugin.msg().send(player, LangKey.HAT_NO_ITEM);
                            return;
                        }
                        ItemStack helmet = player.getInventory().getHelmet();
                        if (helmet == null || helmet.getType() == Material.AIR) {
                            plugin.msg().send(player, LangKey.HAT_NO_ITEM);
                            return;
                        }
                        player.getInventory().setHelmet(null);
                        giveOrDrop(player, helmet);
                        plugin.msg().send(player, LangKey.HAT_SUCCESS);
                        return;
                    }

                    ItemStack newHelmet = hand.clone();
                    newHelmet.setAmount(1);

                    ItemStack oldHelmet = player.getInventory().getHelmet();

                    hand.setAmount(hand.getAmount() - 1);
                    player.getInventory().setItemInMainHand(hand.getAmount() == 0 ? null : hand);
                    player.getInventory().setHelmet(newHelmet);

                    if (oldHelmet != null && oldHelmet.getType() != Material.AIR) {
                        giveOrDrop(player, oldHelmet);
                        if (plugin.config().get().hat().returnOldHelmet()) {
                            plugin.msg().send(player, LangKey.HAT_RETURNED);
                        }
                    }
                    plugin.msg().send(player, LangKey.HAT_SUCCESS);
                })
                .register();
    }

    private void giveOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(rest ->
                    player.getWorld().dropItemNaturally(player.getLocation(), rest));
        }
    }
}