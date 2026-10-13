package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.concurrent.ThreadLocalRandom;

public class RepairCommand {

    public static final String PERM     = "terracraft.core.repair";
    public static final String PERM_ALL = "terracraft.core.repair.all";

    private static final String ACTION_ALL = "repair_all";

    private final TerraCorePlugin plugin;

    public RepairCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // ==== /repair ====
        new CommandAPICommand("repair")
                .withAliases("fix")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.REPAIR)) return;

                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getType() == Material.AIR || !isDamaged(hand)) {
                        plugin.msg().send(player, LangKey.REPAIR_NO_ITEM);
                        return;
                    }

                    AdminToolsConfig.Repair cfg = plugin.config().get().repair();

                    if (cfg.breakChance() > 0 || cfg.repairChance() < 100) {
                        plugin.msg().send(player, LangKey.CHANCE_REPAIR_WARN,
                                "chance", format(cfg.repairChance()),
                                "break",  format(cfg.breakChance()));
                    }

                    Result result = attempt(hand);
                    switch (result) {
                        case REPAIRED -> {
                            plugin.msg().actionBar(player, LangKey.REPAIR_SUCCESS, "value", 1);
                            plugin.msg().sound(player, Sound.BLOCK_ANVIL_USE, 0.7f, 1.2f);
                        }
                        case BROKEN -> {
                            player.getInventory().setItemInMainHand(null);
                            plugin.msg().actionBar(player, LangKey.REPAIR_BROKEN,
                                    "item", hand.getType().name().toLowerCase());
                            plugin.msg().sound(player, Sound.ENTITY_ITEM_BREAK, 0.7f, 1.0f);
                        }
                        case FAILED -> {
                            plugin.msg().actionBar(player, LangKey.REPAIR_FAILED);
                            plugin.msg().sound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
                        }
                    }
                })
                .register();

        // ==== /repair all — подтверждение или сразу ====
        new CommandAPICommand("repair")
                .withPermission(PERM_ALL)
                .withArguments(new LiteralArgument("all"))
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.REPAIR)) return;

                    int damaged = countDamaged(player);
                    if (damaged == 0) {
                        plugin.msg().send(player, LangKey.REPAIR_NO_ITEM);
                        return;
                    }

                    if (plugin.config().get().feedback().confirmations()) {
                        // Подтверждения включены — показываем кнопки
                        plugin.confirms().expect(player, ACTION_ALL);

                        plugin.msg().confirm(player,
                                LangKey.CONFIRM_REPAIR_HEADER,
                                "/repair confirm",
                                "/repair cancel",
                                "count", damaged);

                        plugin.msg().hint(player, LangKey.CONFIRM_HINT);
                    } else {
                        // Подтверждения выключены — сразу выполняем
                        repairAll(player);
                    }
                })
                .register();

        // ==== /repair confirm ====
        new CommandAPICommand("repair")
                .withPermission(PERM_ALL)
                .withArguments(new LiteralArgument("confirm"))
                .executesPlayer((player, args) -> {
                    if (!plugin.confirms().consume(player, ACTION_ALL)) {
                        plugin.msg().send(player, LangKey.CONFIRM_EXPIRED);
                        return;
                    }
                    repairAll(player);
                })
                .register();

        // ==== /repair cancel ====
        new CommandAPICommand("repair")
                .withPermission(PERM_ALL)
                .withArguments(new LiteralArgument("cancel"))
                .executesPlayer((player, args) -> {
                    plugin.confirms().cancel(player, ACTION_ALL);
                    plugin.msg().send(player, LangKey.CONFIRM_CANCELLED);
                })
                .register();
    }

    // ============ Логика ============

    private enum Result { REPAIRED, BROKEN, FAILED }

    private void repairAll(@NotNull Player player) {
        int repaired = 0, broken = 0, failed = 0;

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (!isDamaged(item)) continue;

            Result result = attempt(item);
            switch (result) {
                case REPAIRED -> repaired++;
                case BROKEN -> {
                    player.getInventory().setItem(i, null);
                    broken++;
                }
                case FAILED -> failed++;
            }
        }

        if (repaired == 0 && broken == 0 && failed == 0) {
            plugin.msg().send(player, LangKey.REPAIR_NO_ITEM);
            return;
        }

        plugin.msg().sendRaw(player, LangKey.REPAIR_SUMMARY_HEADER);
        if (repaired > 0)
            plugin.msg().sendRaw(player, LangKey.REPAIR_SUMMARY_REPAIRED, "value", repaired);
        if (broken > 0)
            plugin.msg().sendRaw(player, LangKey.REPAIR_SUMMARY_BROKEN, "value", broken);
        if (failed > 0)
            plugin.msg().sendRaw(player, LangKey.REPAIR_SUMMARY_FAILED, "value", failed);

        plugin.msg().sound(player,
                broken > 0 ? Sound.ENTITY_ITEM_BREAK : Sound.BLOCK_ANVIL_USE, 0.7f, 1.0f);
    }

    private @NotNull Result attempt(@NotNull ItemStack item) {
        AdminToolsConfig.Repair cfg = plugin.config().get().repair();
        if (roll(cfg.breakChance())) return Result.BROKEN;
        if (roll(cfg.repairChance())) {
            repair(item);
            return Result.REPAIRED;
        }
        return Result.FAILED;
    }

    private int countDamaged(@NotNull Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isDamaged(item)) count++;
        }
        return count;
    }

    private boolean roll(double percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return ThreadLocalRandom.current().nextDouble(100.0) < percent;
    }

    private boolean isDamaged(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!(item.getItemMeta() instanceof Damageable dmg)) return false;
        return dmg.getDamage() > 0;
    }

    private boolean repair(ItemStack item) {
        if (!isDamaged(item)) return false;
        Damageable dmg = (Damageable) item.getItemMeta();
        dmg.setDamage(0);
        item.setItemMeta(dmg);
        return true;
    }

    private @NotNull String format(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.1f", d);
    }
}