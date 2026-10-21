package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;

/**
 * /afk          — toggle себе
 * /afk &lt;player&gt; — toggle другому (право {@value #PERM_OTHERS})
 *
 * <p>Кулдауна нет — переключение дёшево и часто используется.
 */
public class AfkCommand {

    public static final String PERM        = "terracraft.core.afk";
    public static final String PERM_OTHERS = "terracraft.core.afk.others";

    private final TerraCorePlugin plugin;

    public AfkCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // ==== /afk (self toggle) ====
        new CommandAPICommand("afk")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    boolean nowAfk = !plugin.afk().isAfk(player.getUniqueId());
                    plugin.afk().set(player, nowAfk);
                    plugin.msg().actionBar(player,
                            nowAfk ? LangKey.AFK_ENABLED : LangKey.AFK_DISABLED);
                    plugin.msg().logAction(player, "/afk", "state", nowAfk ? "on" : "off");
                })
                .register();

        // ==== /afk <player> (other) ====
        new CommandAPICommand("afk")
                .withPermission(PERM_OTHERS)
                .withArguments(Args.playerArg("target"))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }

                    boolean nowAfk = !plugin.afk().isAfk(target.getUniqueId());
                    plugin.afk().set(target, nowAfk);

                    // Сообщение сендеру
                    plugin.msg().send(sender,
                            nowAfk ? LangKey.AFK_OTHER_ENABLED : LangKey.AFK_OTHER_DISABLED,
                            "player", target.getName());

                    // Target'у — если это не тот же игрок
                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target,
                                nowAfk ? LangKey.AFK_ENABLED : LangKey.AFK_DISABLED);
                    }

                    plugin.msg().logAction(sender, "/afk",
                            "player", target.getName(),
                            "state", nowAfk ? "on" : "off");
                })
                .register();
    }
}