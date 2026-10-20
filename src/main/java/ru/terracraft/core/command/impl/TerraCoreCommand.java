package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangManager;
import ru.terracraft.core.lang.LangKey;

import java.util.List;
import java.util.Locale;

/**
 * /terracore <reload|info|lang>
 *
 * <p>Алиасы: /tc, /terra
 */
public class TerraCoreCommand {

    public static final String PERM_USE    = "terracraft.core.use";
    public static final String PERM_RELOAD = "terracraft.core.reload";
    public static final String PERM_INFO   = "terracraft.core.info";
    public static final String PERM_LANG   = "terracraft.core.lang";

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
                .withSubcommand(new CommandAPICommand("lang")
                        .withPermission(PERM_LANG)
                        .executes((sender, args) -> {
                            showLang(sender);
                        }))
                .register();
    }

    // ============ /tc lang ============

    private void showLang(@NotNull org.bukkit.command.CommandSender sender) {
        List<LangManager.LocaleStats> stats = plugin.lang().stats();

        plugin.msg().sendRaw(sender, LangKey.INFO_HEADER);
        sender.sendMessage(Component.text("Загруженные локали: " + stats.size())
                .color(NamedTextColor.GRAY));

        for (LangManager.LocaleStats s : stats) {
            String tag = s.locale().toLanguageTag();
            String type = s.builtin() ? "built-in" : "custom";
            int percent = s.percentComplete();

            NamedTextColor color = s.missingKeys() == 0
                    ? NamedTextColor.GREEN
                    : (percent >= 80 ? NamedTextColor.YELLOW : NamedTextColor.RED);

            String icon = s.missingKeys() == 0 ? "✔" : "⚠";

            Component line = Component.text()
                    .append(Component.text("  " + icon + " ", color))
                    .append(Component.text(tag, NamedTextColor.WHITE))
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(type, NamedTextColor.GRAY))
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(percent + "%", color))
                    .append(Component.text(" (" + (s.totalKeys() - s.missingKeys())
                            + "/" + s.totalKeys() + ")", NamedTextColor.DARK_GRAY))
                    .build();

            sender.sendMessage(line);

            // Если missing — показать первые 5
            if (s.missingKeys() > 0) {
                List<String> missing = plugin.lang().missingKeys(s.locale());
                int show = Math.min(5, missing.size());
                String preview = String.join(", ", missing.subList(0, show));
                if (missing.size() > show) preview += ", … (+" + (missing.size() - show) + ")";

                sender.sendMessage(Component.text("      " + preview, NamedTextColor.DARK_GRAY));
            }
        }

        plugin.msg().sendRaw(sender, LangKey.INFO_HEADER);
    }
}