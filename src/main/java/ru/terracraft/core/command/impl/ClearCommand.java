package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ClearCommand {

    public static final String PERM_SELF   = "terracraft.core.clear";
    public static final String PERM_OTHERS = "terracraft.core.clear.others";

    private final TerraCorePlugin plugin;

    public ClearCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /clear — себе
        new CommandAPICommand("clear")
                .withAliases("clearinventory", "ci")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    player.getInventory().clear();
                    plugin.msg().actionBar(player, LangKey.CLEAR_SUCCESS);
                    plugin.msg().sound(player, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.2f);
                    plugin.msg().logAction(player, "/clear", "target", "self");
                })
                .register();

        // /clear <target>
        new CommandAPICommand("clear")
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

                    target.getInventory().clear();
                    plugin.msg().actionBar(target, LangKey.CLEAR_SUCCESS);
                    plugin.msg().sound(target, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.2f);

                    if (!target.equals(sender)) {
                        plugin.msg().send(sender, LangKey.CLEAR_SUCCESS);
                    }

                    plugin.msg().logAction(sender, "/clear",
                            "target", target.getName());
                })
                .register();
    }
}