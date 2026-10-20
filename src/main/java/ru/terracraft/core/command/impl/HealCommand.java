package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class HealCommand {

    public static final String PERM_SELF   = "terracraft.core.heal";
    public static final String PERM_OTHERS = "terracraft.core.heal.others";

    private static final String ACTION_SELF = "heal_self";

    private final TerraCorePlugin plugin;

    public HealCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("heal")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    AdminToolsConfig.Heal cfg = plugin.config().get().heal();
                    boolean needsConfirm = cfg.healChance() < 100
                            && plugin.config().get().feedback().confirmations();
                    if (needsConfirm) {
                        plugin.confirms().expect(player, ACTION_SELF);
                        plugin.msg().confirm(player,
                                LangKey.CONFIRM_HEAL_HEADER,
                                "/heal confirm",
                                "/heal cancel",
                                "chance", format(cfg.healChance()));
                        plugin.msg().hint(player, LangKey.CONFIRM_HINT);
                        return;
                    }
                    executeSelf(player);
                })
                .register();

        new CommandAPICommand("heal")
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
                    executeOther(sender, target);
                })
                .register();

        new CommandAPICommand("heal")
                .withPermission(PERM_SELF)
                .withArguments(new LiteralArgument("confirm"))
                .executesPlayer((player, args) -> {
                    if (!plugin.confirms().consume(player, ACTION_SELF)) {
                        plugin.msg().send(player, LangKey.CONFIRM_EXPIRED);
                        return;
                    }
                    executeSelf(player);
                })
                .register();

        new CommandAPICommand("heal")
                .withPermission(PERM_SELF)
                .withArguments(new LiteralArgument("cancel"))
                .executesPlayer((player, args) -> {
                    plugin.confirms().cancel(player, ACTION_SELF);
                    plugin.msg().send(player, LangKey.CONFIRM_CANCELLED);
                })
                .register();
    }

    private void executeSelf(@NotNull Player player) {
        if (!plugin.cooldowns().tryAcquire(player, CommandKey.HEAL)) return;

        AdminToolsConfig.Heal cfg = plugin.config().get().heal();
        if (cfg.healChance() < 100) {
            plugin.msg().send(player, LangKey.CHANCE_HEAL_WARN,
                    "chance", format(cfg.healChance()));
        }

        double healed = heal(player);
        if (healed < 0) {
            plugin.cooldowns().rollback(player, CommandKey.HEAL);
            plugin.msg().actionBar(player, LangKey.HEAL_FAILED);
            plugin.msg().sound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }

        plugin.cooldowns().commit(player, CommandKey.HEAL);
        plugin.msg().actionBar(player, LangKey.HEAL_SELF);
        plugin.msg().actionBar(player, LangKey.HEAL_AMOUNT, "value", formatHearts(healed));
        plugin.msg().sound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
        plugin.msg().logAction(player, "/heal", "target", "self", "healed", formatHearts(healed));
        plugin.stats().increment(player, "heal");
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull Player target) {
        Player senderPlayer = (sender instanceof Player p) ? p : null;
        if (senderPlayer != null
                && !plugin.cooldowns().tryAcquire(senderPlayer, CommandKey.HEAL)) {
            return;
        }

        AdminToolsConfig.Heal cfg = plugin.config().get().heal();
        if (cfg.healChance() < 100) {
            plugin.msg().send(sender, LangKey.CHANCE_HEAL_WARN,
                    "chance", format(cfg.healChance()));
        }

        double healed = heal(target);
        if (healed < 0) {
            if (senderPlayer != null) plugin.cooldowns().rollback(senderPlayer, CommandKey.HEAL);
            plugin.msg().send(sender, LangKey.HEAL_FAILED);
            return;
        }

        if (senderPlayer != null) plugin.cooldowns().commit(senderPlayer, CommandKey.HEAL);
        plugin.msg().send(sender, LangKey.HEAL_OTHER, "player", target.getName());
        plugin.msg().send(sender, LangKey.HEAL_AMOUNT, "value", formatHearts(healed));

        if (!target.equals(sender)) {
            plugin.msg().actionBar(target, LangKey.HEAL_RECEIVED);
            plugin.msg().actionBar(target, LangKey.HEAL_AMOUNT, "value", formatHearts(healed));
            plugin.msg().sound(target, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
        }
        plugin.msg().logAction(sender, "/heal", "target", target.getName(), "healed", formatHearts(healed));
        if (senderPlayer != null) plugin.stats().increment(senderPlayer, "heal");
    }

    private double heal(@NotNull Player player) {
        AdminToolsConfig.Heal cfg = plugin.config().get().heal();
        if (!roll(cfg.healChance())) return -1;

        AttributeInstance maxAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxAttr != null ? maxAttr.getValue() : 20.0;
        double maxHearts = maxHealth / 2.0;

        int minHearts = Math.max(1, Math.min(cfg.minHearts(), (int) maxHearts));
        int hearts = ThreadLocalRandom.current().nextInt(minHearts, (int) maxHearts + 1);

        double newHealth = Math.min(hearts * 2.0, maxHealth);
        double oldHealth = player.getHealth();
        player.setHealth(newHealth);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        return (newHealth - oldHealth) / 2.0;
    }

    private boolean roll(double percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return ThreadLocalRandom.current().nextDouble(100.0) < percent;
    }

    private @NotNull String formatHearts(double hearts) {
        if (hearts == Math.floor(hearts)) return String.valueOf((int) hearts);
        return String.format("%.1f", hearts);
    }

    private @NotNull String format(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.1f", d);
    }
}