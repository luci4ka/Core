package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.util.concurrent.ThreadLocalRandom;

public class FeedCommand {

    public static final String PERM_SELF   = "terracraft.core.feed";
    public static final String PERM_OTHERS = "terracraft.core.feed.others";

    private static final int MAX_FOOD = 20;

    private final TerraCorePlugin plugin;

    public FeedCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /feed — себе
        new CommandAPICommand("feed")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (!plugin.cooldowns().check(player, CommandKey.FEED)) return;

                    AdminToolsConfig.Feed cfg = plugin.config().get().feed();
                    if (cfg.feedChance() < 100) {
                        plugin.msg().send(player, LangKey.CHANCE_FEED_WARN,
                                "chance", format(cfg.feedChance()));
                    }

                    int fed = feed(player);
                    if (fed < 0) {
                        plugin.msg().actionBar(player, LangKey.FEED_FAILED);
                        plugin.msg().sound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
                        return;
                    }

                    plugin.msg().actionBar(player, LangKey.FEED_SELF);
                    plugin.msg().actionBar(player, LangKey.FEED_AMOUNT, "value", fed);
                    plugin.msg().sound(player, Sound.ENTITY_PLAYER_BURP, 0.6f, 1.0f);
                })
                .register();

        // /feed <target> — другому
        new CommandAPICommand("feed")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    apply(sender, target);
                })
                .register();
    }

    private void apply(@NotNull CommandSender sender, @NotNull Player target) {
        AdminToolsConfig.Feed cfg = plugin.config().get().feed();
        if (cfg.feedChance() < 100) {
            plugin.msg().send(sender, LangKey.CHANCE_FEED_WARN,
                    "chance", format(cfg.feedChance()));
        }

        int fed = feed(target);
        if (fed < 0) {
            plugin.msg().send(sender, LangKey.FEED_FAILED);
            return;
        }

        plugin.msg().send(sender, LangKey.FEED_OTHER, "player", target.getName());
        plugin.msg().send(sender, LangKey.FEED_AMOUNT, "value", fed);

        if (!target.equals(sender)) {
            plugin.msg().actionBar(target, LangKey.FEED_RECEIVED);
            plugin.msg().actionBar(target, LangKey.FEED_AMOUNT, "value", fed);
            plugin.msg().sound(target, Sound.ENTITY_PLAYER_BURP, 0.6f, 1.0f);
        }
    }

    private int feed(@NotNull Player player) {
        AdminToolsConfig.Feed cfg = plugin.config().get().feed();
        if (!roll(cfg.feedChance())) return -1;

        int min = Math.max(0, Math.min(cfg.minFoodLevel(), MAX_FOOD));
        int newFood = ThreadLocalRandom.current().nextInt(min, MAX_FOOD + 1);
        int oldFood = player.getFoodLevel();

        player.setFoodLevel(newFood);
        player.setSaturation(newFood);
        return newFood - oldFood;
    }

    private boolean roll(double percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return ThreadLocalRandom.current().nextDouble(100.0) < percent;
    }

    private @NotNull String format(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.1f", d);
    }
}