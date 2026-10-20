package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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

public class FeedCommand {

    public static final String PERM_SELF   = "terracraft.core.feed";
    public static final String PERM_OTHERS = "terracraft.core.feed.others";

    private static final int MAX_FOOD = 20;
    private static final String ACTION_SELF = "feed_self";

    private final TerraCorePlugin plugin;

    public FeedCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("feed")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    AdminToolsConfig.Feed cfg = plugin.config().get().feed();
                    boolean needsConfirm = cfg.feedChance() < 100
                            && plugin.config().get().feedback().confirmations();
                    if (needsConfirm) {
                        plugin.confirms().expect(player, ACTION_SELF);
                        plugin.msg().confirm(player,
                                LangKey.CONFIRM_FEED_HEADER,
                                "/feed confirm",
                                "/feed cancel",
                                "chance", format(cfg.feedChance()));
                        plugin.msg().hint(player, LangKey.CONFIRM_HINT);
                        return;
                    }
                    executeSelf(player);
                })
                .register();

        new CommandAPICommand("feed")
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

        new CommandAPICommand("feed")
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

        new CommandAPICommand("feed")
                .withPermission(PERM_SELF)
                .withArguments(new LiteralArgument("cancel"))
                .executesPlayer((player, args) -> {
                    plugin.confirms().cancel(player, ACTION_SELF);
                    plugin.msg().send(player, LangKey.CONFIRM_CANCELLED);
                })
                .register();
    }

    private void executeSelf(@NotNull Player player) {
        if (!plugin.cooldowns().tryAcquire(player, CommandKey.FEED)) return;

        AdminToolsConfig.Feed cfg = plugin.config().get().feed();
        if (cfg.feedChance() < 100) {
            plugin.msg().send(player, LangKey.CHANCE_FEED_WARN, "chance", format(cfg.feedChance()));
        }

        int fed = feed(player);
        if (fed < 0) {
            plugin.cooldowns().rollback(player, CommandKey.FEED);
            plugin.msg().actionBar(player, LangKey.FEED_FAILED);
            plugin.msg().sound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }

        plugin.cooldowns().commit(player, CommandKey.FEED);
        plugin.msg().actionBar(player, LangKey.FEED_SELF);
        plugin.msg().actionBar(player, LangKey.FEED_AMOUNT, "value", fed);
        plugin.msg().sound(player, Sound.ENTITY_PLAYER_BURP, 0.6f, 1.0f);
        plugin.msg().logAction(player, "/feed", "target", "self", "fed", fed);
        plugin.stats().increment(player, "feed");
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull Player target) {
        Player senderPlayer = (sender instanceof Player p) ? p : null;
        if (senderPlayer != null
                && !plugin.cooldowns().tryAcquire(senderPlayer, CommandKey.FEED)) {
            return;
        }

        AdminToolsConfig.Feed cfg = plugin.config().get().feed();
        if (cfg.feedChance() < 100) {
            plugin.msg().send(sender, LangKey.CHANCE_FEED_WARN, "chance", format(cfg.feedChance()));
        }

        int fed = feed(target);
        if (fed < 0) {
            if (senderPlayer != null) plugin.cooldowns().rollback(senderPlayer, CommandKey.FEED);
            plugin.msg().send(sender, LangKey.FEED_FAILED);
            return;
        }

        if (senderPlayer != null) plugin.cooldowns().commit(senderPlayer, CommandKey.FEED);
        plugin.msg().send(sender, LangKey.FEED_OTHER, "player", target.getName());
        plugin.msg().send(sender, LangKey.FEED_AMOUNT, "value", fed);

        if (!target.equals(sender)) {
            plugin.msg().actionBar(target, LangKey.FEED_RECEIVED);
            plugin.msg().actionBar(target, LangKey.FEED_AMOUNT, "value", fed);
            plugin.msg().sound(target, Sound.ENTITY_PLAYER_BURP, 0.6f, 1.0f);
        }
        plugin.msg().logAction(sender, "/feed", "target", target.getName(), "fed", fed);
        if (senderPlayer != null) plugin.stats().increment(senderPlayer, "feed");
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