package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

public class GodCommand {

    public static final String PERM_SELF   = "terracraft.core.god";
    public static final String PERM_OTHERS = "terracraft.core.god.others";

    private final TerraCorePlugin plugin;

    public GodCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /god — себе
        new CommandAPICommand("god")
                .withAliases("godmode", "invul")
                .withPermission(PERM_SELF)
                .executesPlayer((player, args) -> {
                    boolean nowOn = plugin.gods().toggle(player);
                    plugin.msg().actionBar(player,
                            nowOn ? LangKey.GOD_ENABLED_SELF : LangKey.GOD_DISABLED_SELF);
                    plugin.msg().sound(player,
                            nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                            0.7f, 1.0f);
                })
                .register();

        // /god <target> — другому
        new CommandAPICommand("god")
                .withPermission(PERM_OTHERS)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;

                    boolean nowOn = plugin.gods().toggle(target);
                    plugin.msg().send(sender,
                            nowOn ? LangKey.GOD_ENABLED_OTHER : LangKey.GOD_DISABLED_OTHER,
                            "player", target.getName());

                    if (!target.equals(sender)) {
                        plugin.msg().actionBar(target,
                                nowOn ? LangKey.GOD_RECEIVED_ENABLED : LangKey.GOD_RECEIVED_DISABLED);
                        plugin.msg().sound(target,
                                nowOn ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE,
                                0.7f, 1.0f);
                    }
                })
                .register();
    }
}