package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.concurrent.ThreadLocalRandom;

public class HealCommand {

    public static final String PERM_SELF   = "terracraft.core.heal";
    public static final String PERM_OTHERS = "terracraft.core.heal.others";

    private final TerraCorePlugin plugin;

    public HealCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /heal — себе
        new CommandAPICommand("heal")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.HEAL)) return;

                    AdminToolsConfig.Heal cfg = plugin.config().get().heal();
                    if (cfg.healChance() < 100) {
                        plugin.msg().send(player, LangKey.CHANCE_HEAL_WARN,
                                "chance", format(cfg.healChance()));
                    }

                    double healed = heal(player);
                    if (healed < 0) {
                        plugin.msg().actionBar(player, LangKey.HEAL_FAILED);
                        plugin.msg().sound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
                        return;
                    }

                    plugin.msg().actionBar(player, LangKey.HEAL_SELF);
                    plugin.msg().actionBar(player, LangKey.HEAL_AMOUNT,
                            "value", formatHearts(healed));
                    plugin.msg().sound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                })
                .register();

        // /heal <target> — другому
        new CommandAPICommand("heal")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    AdminToolsConfig.Heal cfg = plugin.config().get().heal();
                    if (cfg.healChance() < 100) {
                        plugin.msg().send(sender, LangKey.CHANCE_HEAL_WARN,
                                "chance", format(cfg.healChance()));
                    }

                    double healed = heal(target);
                    if (healed < 0) {
                        plugin.msg().send(sender, LangKey.HEAL_FAILED);
                        return;
                    }

                    plugin.msg().send(sender, LangKey.HEAL_OTHER,
                            "player", target.getName());
                    plugin.msg().send(sender, LangKey.HEAL_AMOUNT,
                            "value", formatHearts(healed));

                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target, LangKey.HEAL_RECEIVED);
                        plugin.msg().actionBar(target, LangKey.HEAL_AMOUNT,
                                "value", formatHearts(healed));
                        plugin.msg().sound(target, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                    }
                })
                .register();
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