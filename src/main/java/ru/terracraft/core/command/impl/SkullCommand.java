package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

public class SkullCommand {

    public static final String PERM = "terracraft.core.skull";
    public static final String PERM_OTHERS = "terracraft.core.skull.others";

    private final TerraCorePlugin plugin;

    public SkullCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("skull")
                .withAliases("head")
                .withPermission(PERM)
                .withOptionalArguments(Args.playerArg("target"))
                .executesPlayer((player, args) -> {
                    String rawName = (String) args.get("target");

                    if (rawName == null || rawName.isBlank()) {
                        giveSkull(player, player);
                        return;
                    }

                    if (!player.hasPermission(PERM_OTHERS)) {
                        plugin.msg().send(player, LangKey.NO_PERMISSION);
                        return;
                    }

                    Player online = Bukkit.getPlayerExact(rawName);
                    if (online != null) {
                        giveSkull(player, online);
                        return;
                    }

                    OfflinePlayer offline = Bukkit.getOfflinePlayer(rawName);
                    if (!offline.hasPlayedBefore() && !offline.isOnline()) {
                        plugin.msg().send(player, LangKey.PLAYER_NOT_FOUND,
                                "player", rawName);
                        return;
                    }
                    giveSkullOffline(player, offline, rawName);
                })
                .register();
    }

    private void giveSkull(@NotNull Player receiver, @NotNull Player target) {
        ItemStack skull = createSkull(target.getName(), target);
        if (skull == null) return;

        var leftover = receiver.getInventory().addItem(skull);
        leftover.values().forEach(rest ->
                receiver.getWorld().dropItemNaturally(receiver.getLocation(), rest));

        plugin.msg().actionBar(receiver, LangKey.SKULL_SUCCESS,
                "player", target.getName());
        plugin.msg().sound(receiver, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
        plugin.msg().logAction(receiver, "/skull", "player", target.getName());
    }

    private void giveSkullOffline(@NotNull Player receiver,
                                  @NotNull OfflinePlayer offline,
                                  @NotNull String fallbackName) {
        String name = offline.getName() != null ? offline.getName() : fallbackName;

        ItemStack skull = createSkull(name, offline);
        if (skull == null) return;

        var leftover = receiver.getInventory().addItem(skull);
        leftover.values().forEach(rest ->
                receiver.getWorld().dropItemNaturally(receiver.getLocation(), rest));

        plugin.msg().send(receiver, LangKey.SKULL_SUCCESS, "player", name);
        plugin.msg().sound(receiver, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
        plugin.msg().logAction(receiver, "/skull", "player", name, "offline", true);
    }

    private ItemStack createSkull(@NotNull String displayName,
                                  @NotNull OfflinePlayer owner) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return null;

        meta.setOwningPlayer(owner);
        meta.displayName(Component.text(displayName));
        skull.setItemMeta(meta);
        return skull;
    }
}