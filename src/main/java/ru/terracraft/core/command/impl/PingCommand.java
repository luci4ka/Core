package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * /ping [player]
 *
 * <p>Без аргумента — свой пинг. С аргументом — пинг другого игрока.
 *
 * <p>Цвет пинга:
 * <ul>
 *   <li>{@code < goodThreshold} — зелёный;</li>
 *   <li>{@code goodThreshold..badThreshold} — жёлтый;</li>
 *   <li>{@code >= badThreshold} — красный.</li>
 * </ul>
 *
 * <p>Пороги — в {@code config.yml}, раздел {@code ping}.
 */
public class PingCommand {

    public static final String PERM_SELF   = "terracraft.core.ping";
    public static final String PERM_OTHERS = "terracraft.core.ping.others";

    private final TerraCorePlugin plugin;

    public PingCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /ping — себе
        new CommandAPICommand("ping")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    send(player, player);
                })
                .register();

        // /ping <target>
        new CommandAPICommand("ping")
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
                    send(sender, target);
                })
                .register();
    }

    // ============ Отправка ============

    private void send(@NotNull CommandSender sender, @NotNull Player target) {
        int ping = target.getPing();
        boolean self = target.equals(sender);

        LangKey key = self ? pickSelfKey(ping) : pickOtherKey(ping);

        if (self) {
            plugin.msg().send(sender, key, "value", ping);
        } else {
            plugin.msg().send(sender, key,
                    "player", target.getName(),
                    "value", ping);
        }
    }

    // ============ Выбор ключа по порогу ============

    private @NotNull LangKey pickSelfKey(int ping) {
        AdminToolsConfig.Ping cfg = plugin.config().get().ping();
        if (ping < cfg.goodThreshold()) return LangKey.PING_SELF_GOOD;
        if (ping < cfg.badThreshold())  return LangKey.PING_SELF_MEDIUM;
        return LangKey.PING_SELF_BAD;
    }

    private @NotNull LangKey pickOtherKey(int ping) {
        AdminToolsConfig.Ping cfg = plugin.config().get().ping();
        if (ping < cfg.goodThreshold()) return LangKey.PING_OTHER_GOOD;
        if (ping < cfg.badThreshold())  return LangKey.PING_OTHER_MEDIUM;
        return LangKey.PING_OTHER_BAD;
    }
}