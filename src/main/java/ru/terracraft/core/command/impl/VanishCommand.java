package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
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

public class VanishCommand {

    public static final String PERM_SELF   = "terracraft.core.vanish";
    public static final String PERM_OTHERS = "terracraft.core.vanish.others";

    private final TerraCorePlugin plugin;

    public VanishCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /vanish
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
                })
                .register();

        // /vanish list — с кликабельными никами
        new CommandAPICommand("vanish")
                .withSubcommand(new CommandAPICommand("list")
                        .withPermission(VanishManager.PERM_SEE)
                        .executes((sender, args) -> {
                            var all = plugin.vanish().all();
                            if (all.isEmpty()) {
                                plugin.msg().send(sender, LangKey.VANISH_LIST_EMPTY);
                                return;
                            }

                            plugin.msg().sendRaw(sender, LangKey.VANISH_LIST_HEADER,
                                    "value", all.size());

                            for (var id : all) {
                                Player p = Bukkit.getPlayer(id);
                                if (p == null) continue;

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

        // /vanish <target>
        new CommandAPICommand("vanish")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
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
                })
                .register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().vanishDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}