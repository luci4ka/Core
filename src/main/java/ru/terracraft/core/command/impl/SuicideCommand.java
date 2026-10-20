package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;

/**
 * /suicide — убить себя.
 *
 * <p>Право: terracraft.core.suicide (default: true)
 *
 * <p>Не срабатывает в креативе/спектаторе и в god-режиме:
 * урон там не проходит, поэтому сообщение «успех» было бы ложью.
 */
public class SuicideCommand {

    public static final String PERM = "terracraft.core.suicide";

    private final TerraCorePlugin plugin;

    public SuicideCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("suicide")
                .withAliases("kill", "die")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    GameMode mode = player.getGameMode();
                    if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                        plugin.msg().send(player, LangKey.SUICIDE_DENIED,
                                "value", localizedMode(mode, player));
                        return;
                    }
                    if (plugin.gods().isGod(player)) {
                        // Локализованное имя godmode — вместо сырого "god"
                        String godName = plugin.lang().raw(player.locale(), LangKey.GOD_MODE_NAME);
                        if (godName == null) godName = "god";
                        plugin.msg().send(player, LangKey.SUICIDE_DENIED, "value", godName);
                        return;
                    }

                    plugin.msg().send(player, LangKey.SUICIDE_SUCCESS);
                    player.setHealth(0);
                    plugin.msg().logAction(player, "/suicide");
                    plugin.stats().increment(player, "suicide");
                })
                .register();
    }

    /** GameMode → локализованное имя через GAMEMODE_NAME_*. */
    private @NotNull String localizedMode(@NotNull GameMode mode, @NotNull Player player) {
        LangKey key = switch (mode) {
            case SURVIVAL  -> LangKey.GAMEMODE_NAME_SURVIVAL;
            case CREATIVE  -> LangKey.GAMEMODE_NAME_CREATIVE;
            case ADVENTURE -> LangKey.GAMEMODE_NAME_ADVENTURE;
            case SPECTATOR -> LangKey.GAMEMODE_NAME_SPECTATOR;
        };
        String s = plugin.lang().raw(player.locale(), key);
        return s != null ? s : mode.name().toLowerCase(Locale.ROOT);
    }
}