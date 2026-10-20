package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /terracore <reload|info>
 *
 * <p>Алиасы: /tc, /terra
 */
public class TerraCoreCommand {

    public static final String PERM_USE    = "terracraft.core.use";
    public static final String PERM_RELOAD = "terracraft.core.reload";
    public static final String PERM_INFO   = "terracraft.core.info";

    private final TerraCorePlugin plugin;

    public TerraCoreCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("terracore")
                .withAliases("tc", "terra")
                .withPermission(PERM_USE)
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(PERM_RELOAD)
                        .executes((sender, args) -> {
                            try {
                                plugin.reloadAll();
                                plugin.msg().send(sender, LangKey.RELOADED);
                            } catch (Exception e) {
                                plugin.msg().send(sender, LangKey.RELOAD_FAILED,
                                        "value", e.getMessage());
                                plugin.msg().logAction(sender, "/tc reload");

                                if (sender instanceof Player p) plugin.stats().increment(p, "reload");
                                plugin.getLogger().warning("Ошибка reload: " + e.getMessage());
                            }
                        }))
                .withSubcommand(new CommandAPICommand("info")
                        .withPermission(PERM_INFO)
                        .executes((sender, args) -> {
                            // Paper 1.21+: getPluginMeta() вместо getDescription()
                            String version = plugin.getPluginMeta().getVersion();
                            String server  = Bukkit.getName() + " " + Bukkit.getVersion();
                            String authors = String.join(", ",
                                    plugin.getPluginMeta().getAuthors());

                            plugin.msg().sendRaw(sender, LangKey.INFO_HEADER);
                            plugin.msg().sendRaw(sender, LangKey.INFO_VERSION, "value", version);
                            plugin.msg().sendRaw(sender, LangKey.INFO_SERVER,  "value", server);
                            plugin.msg().sendRaw(sender, LangKey.INFO_AUTHORS, "value", authors);
                            plugin.msg().sendRaw(sender, LangKey.INFO_HEADER);
                        }))
                .register();
    }
}