package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;
import ru.terracraft.core.manager.VanishManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class VanishCommand {

    public static final String PERM_SELF   = "terracraft.core.vanish";
    public static final String PERM_OTHERS = "terracraft.core.vanish.others";

    private final TerraCorePlugin plugin;

    public VanishCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("vanish")
                .withAliases("v")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    if (isWorldDisabled(player)) {
                        plugin.msg().send(player, LangKey.VANISH_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.VANISH)) return;

                    boolean nowHidden = plugin.vanish().toggle(player);
                    plugin.msg().actionBar(player,
                            nowHidden ? LangKey.VANISH_ENABLED : LangKey.VANISH_DISABLED);
                    plugin.msg().sound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);
                    plugin.msg().logAction(player, "/vanish",
                            "player", player.getName(),
                            "state", nowHidden ? "on" : "off");
                    plugin.stats().increment(player, "vanish");
                })
                .register();

        new CommandAPICommand("vanish")
                .withSubcommand(new CommandAPICommand("list")
                        .withPermission(VanishManager.PERM_SEE)
                        .executes((sender, args) -> {
                            var all = plugin.vanish().all();
                            if (all.isEmpty()) {
                                plugin.msg().send(sender, LangKey.VANISH_LIST_EMPTY);
                                return;
                            }

                            var online = new ArrayList<Player>();
                            for (var id : all) {
                                Player p = Bukkit.getPlayer(id);
                                if (p != null) online.add(p);
                            }

                            if (online.isEmpty()) {
                                plugin.msg().send(sender, LangKey.VANISH_LIST_EMPTY);
                                plugin.getLogger().warning(
                                        "VanishManager содержит " + all.size()
                                                + " офлайн-игроков — почистите их при PlayerQuitEvent");
                                return;
                            }

                            plugin.msg().sendRaw(sender, LangKey.VANISH_LIST_HEADER,
                                    "value", online.size());

                            for (Player p : online) {
                                Component entry = plugin.msg().mini().deserialize(
                                                "<dark_gray>  • <white>" + p.getName())
                                        .hoverEvent(HoverEvent.showText(
                                                Component.text("Нажми, чтобы телепортироваться")
                                                        .color(NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.runCommand("/tp " + p.getName()));

                                sender.sendMessage(entry);
                            }
                        }))
                .register();

        new CommandAPICommand("vanish")
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
                    if (isWorldDisabled(target)) {
                        plugin.msg().send(sender, LangKey.VANISH_DISABLED_WORLD);
                        return;
                    }

                    boolean nowHidden = plugin.vanish().toggle(target);
                    plugin.msg().send(sender,
                            nowHidden ? LangKey.VANISH_OTHER_ENABLED : LangKey.VANISH_OTHER_DISABLED,
                            "player", target.getName());

                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target,
                                nowHidden ? LangKey.VANISH_ENABLED : LangKey.VANISH_DISABLED);
                        plugin.msg().sound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);
                    }
                    plugin.msg().logAction(sender, "/vanish",
                            "player", target.getName(),
                            "state", nowHidden ? "on" : "off");
                    if (sender instanceof Player p) plugin.stats().increment(p, "vanish");
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().vanishDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}