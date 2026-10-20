package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

/**
 * Универсальная команда для открытия верстаков.
 *
 * <p>Использование в {@code CommandRegistrar}:
 * <pre>
 *   new WorkstationCommand(plugin, "workbench",   "wb",       MenuType.CRAFTING,     LangKey.WORKBENCH_OPENED, LangKey.WORKBENCH_TITLE).register();
 *   new WorkstationCommand(plugin, "anvil",       null,       MenuType.ANVIL,        LangKey.ANVIL_OPENED,     LangKey.ANVIL_TITLE).register();
 *   new WorkstationCommand(plugin, "loom",        null,       MenuType.LOOM,         LangKey.LOOM_OPENED,      LangKey.LOOM_TITLE).register();
 *   new WorkstationCommand(plugin, "grindstone",  null,       MenuType.GRINDSTONE,   LangKey.GRINDSTONE_OPENED,LangKey.GRINDSTONE_TITLE).register();
 *   new WorkstationCommand(plugin, "stonecutter", "stonecut", MenuType.STONECUTTER,  LangKey.STONECUTTER_OPENED,LangKey.STONECUTTER_TITLE).register();
 *   new WorkstationCommand(plugin, "smithingtable","smithing", MenuType.SMITHING,     LangKey.SMITHINGTABLE_OPENED,LangKey.SMITHINGTABLE_TITLE).register();
 * </pre>
 */
public class WorkstationCommand {

    private final TerraCorePlugin plugin;
    private final String commandName;
    private final @Nullable String alias;
    private final MenuType menuType;
    private final LangKey openedKey;
    private final LangKey titleKey;

    public WorkstationCommand(@NotNull TerraCorePlugin plugin,
                              @NotNull String commandName,
                              @Nullable String alias,
                              @NotNull MenuType menuType,
                              @NotNull LangKey openedKey,
                              @NotNull LangKey titleKey) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.alias = alias;
        this.menuType = menuType;
        this.openedKey = openedKey;
        this.titleKey = titleKey;
    }

    public void register() {
        CommandAPICommand cmd = new CommandAPICommand(commandName)
                .withPermission("terracraft.core." + commandName)
                .executesPlayer((player, args) -> {
                    if (isWorldDisabled(player)) {
                        plugin.msg().send(player, LangKey.WORKBENCH_DISABLED_WORLD);
                        return;
                    }
                    if (!plugin.cooldowns().check(player, CommandKey.WORKBENCH)) return;

                    // Локализованный заголовок GUI — по локали игрока
                    Component title = plugin.msg().component(player, titleKey);
                    player.openInventory(menuType.create(player, title));

                    // Сообщение — action bar (игрок и так видит GUI, чат не нужен)
                    plugin.msg().actionBar(player, openedKey);
                    plugin.msg().sound(player,
                            Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.6f, 1.2f);
                    plugin.msg().logAction(player, "/" + commandName);
                });

        if (alias != null) {
            cmd.withAliases(alias);
        }

        cmd.register();
    }

    private boolean isWorldDisabled(@NotNull Player p) {
        return plugin.config().get().worlds().workbenchDisabledWorlds()
                .contains(p.getWorld().getName());
    }
}