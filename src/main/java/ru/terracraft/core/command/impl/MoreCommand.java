package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /more — пополнить стак в руке до максимума.
 *
 * Право: terracraft.core.more
 */
public class MoreCommand {

    public static final String PERM = "terracraft.core.more";

    private final TerraCorePlugin plugin;

    public MoreCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("more")
                .withAliases("stack")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    ItemStack item = player.getInventory().getItemInMainHand();

                    if (item.getType() == Material.AIR) {
                        plugin.msg().send(player, LangKey.MORE_NO_ITEM);
                        return;
                    }

                    int max = item.getMaxStackSize();
                    if (item.getAmount() >= max) {
                        plugin.msg().send(player, LangKey.MORE_ALREADY_FULL);
                        return;
                    }

                    item.setAmount(max);

                    // Чат — информация важна дольше 2 секунд
                    plugin.msg().send(player, LangKey.MORE_SUCCESS, "value", max);
                    plugin.msg().sound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    plugin.msg().logAction(player, "/more", "value", max);
                })
                .register();
    }
}