package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Collection;

/**
 * /list
 *
 * Показывает список игроков онлайн.
 *
 * Право: terracraft.core.list
 */
public class ListCommand {

    public static final String PERM = "terracraft.core.list";

    private final TerraCorePlugin plugin;

    public ListCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("list")
                .withAliases("online", "who")
                .withPermission(PERM)
                .executes((sender, args) -> {
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                    if (players.isEmpty()) {
                        plugin.msg().send(sender, LangKey.LIST_EMPTY);
                        return;
                    }

                    plugin.msg().send(sender, LangKey.LIST_HEADER,
                            "value", players.size());

                    for (Player p : players) {
                        plugin.msg().sendRaw(sender, LangKey.LIST_ENTRY,
                                "player", p.getName());
                    }
                })
                .register();
    }
}