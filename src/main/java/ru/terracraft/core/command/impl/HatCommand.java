package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * /hat [player] — надеть блок из руки на голову (или снять шлем пустой рукой).
 *
 * <p>Права:
 * <ul>
 *   <li>{@link #PERM} — себе;</li>
 *   <li>{@link #PERM_OTHERS} — другому.</li>
 * </ul>
 *
 * <p>На голову можно надеть <b>только блок</b> ({@link Material#isBlock()}).
 * Шлемы, мечи, еда, инструменты — отказ: для брони есть ванильный слот.
 */
public class HatCommand {

    public static final String PERM = "terracraft.core.hat";
    public static final String PERM_OTHERS = "terracraft.core.hat.others";

    private final TerraCorePlugin plugin;

    public HatCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /hat — себе
        new CommandAPICommand("hat")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.HAT)) return;
                    apply(player, player);
                })
                .register();

        // /hat <target> — другому
        new CommandAPICommand("hat")
                .withPermission(PERM_OTHERS)
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
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    if (sender instanceof Player p
                            && !plugin.cooldowns().check(p, CommandKey.HAT)) {
                        return;
                    }
                    apply(sender, target);
                })
                .register();
    }

    // ============ Логика ============

    private void apply(@NotNull org.bukkit.command.CommandSender sender,
                       @NotNull Player target) {
        ItemStack hand = target.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            handleEmptyHand(sender, target);
        } else {
            handleWear(sender, target, hand);
        }
    }

    /** Пустая рука: снять шлем, если разрешено. */
    private void handleEmptyHand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull Player target) {
        ItemStack helmet = target.getInventory().getHelmet();
        if (helmet == null || helmet.getType() == Material.AIR) {
            plugin.msg().send(sender, LangKey.HAT_NO_ITEM);
            return;
        }

        AdminToolsConfig.Hat cfg = plugin.config().get().hat();
        if (!cfg.allowEmptyHand()) {
            plugin.msg().send(sender, LangKey.HAT_CANNOT_REMOVE);
            return;
        }

        target.getInventory().setHelmet(null);
        giveOrDrop(target, helmet);

        plugin.msg().actionBar(target, LangKey.HAT_REMOVED);
        if (!target.equals(sender)) {
            plugin.msg().send(sender, LangKey.HAT_REMOVED);
        }

        plugin.msg().sound(target, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6f, 1.2f);
        plugin.msg().logAction(sender, "/hat", "target", target.getName(), "action", "remove");
    }

    /** В руке предмет: надеть его на голову, если это блок. */
    private void handleWear(@NotNull org.bukkit.command.CommandSender sender,
                            @NotNull Player target,
                            @NotNull ItemStack hand) {

        // === Только блоки ===
        if (!hand.getType().isBlock()) {
            plugin.msg().send(sender, LangKey.HAT_NOT_BLOCK);
            return;
        }

        ItemStack newHelmet = hand.clone();
        newHelmet.setAmount(1);

        ItemStack oldHelmet = target.getInventory().getHelmet();

        int newAmount = hand.getAmount() - 1;
        target.getInventory().setItemInMainHand(newAmount <= 0 ? null : hand.asQuantity(newAmount));

        target.getInventory().setHelmet(newHelmet);

        if (oldHelmet != null && oldHelmet.getType() != Material.AIR) {
            giveOrDrop(target, oldHelmet);
            AdminToolsConfig.Hat cfg = plugin.config().get().hat();
            if (cfg.returnOldHelmet()) {
                plugin.msg().actionBar(target, LangKey.HAT_RETURNED);
                if (!target.equals(sender)) {
                    plugin.msg().send(sender, LangKey.HAT_RETURNED);
                }
            }
        }

        plugin.msg().actionBar(target, LangKey.HAT_SUCCESS);
        if (!target.equals(sender)) {
            plugin.msg().send(sender, LangKey.HAT_SUCCESS);
        }

        // Звук — свой для блоков
        plugin.msg().sound(target, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.6f, 1.2f);

        plugin.msg().logAction(sender, "/hat", "target", target.getName(), "action", "wear");
    }

    // ============ Утилиты ============

    /** Кладёт в инвентарь, остаток — дропает под ноги. */
    private void giveOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(rest ->
                    player.getWorld().dropItemNaturally(player.getLocation(), rest));
        }
    }
}