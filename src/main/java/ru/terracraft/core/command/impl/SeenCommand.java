package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /seen <player>
 *
 * Показывает, когда игрок последний раз был онлайн.
 * Работает и для офлайн-игроков.
 *
 * Право: terracraft.core.seen
 */
public class SeenCommand {

    public static final String PERM = "terracraft.core.seen";

    private final TerraCorePlugin plugin;

    public SeenCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("seen")
                .withPermission(PERM)
                .withArguments(new StringArgument("name")
                        .replaceSuggestions((info, builder) -> {
                            String prefix = info.currentInput().toLowerCase();
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase().startsWith(prefix)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return java.util.concurrent.CompletableFuture
                                    .completedFuture(builder.build());
                        }))
                .executes((sender, args) -> {
                    String name = (String) args.get("name");

                    // Сначала — онлайн
                    Player online = Bukkit.getPlayerExact(name);
                    if (online != null) {
                        plugin.msg().send(sender, LangKey.SEEN_ONLINE,
                                "player", online.getName());
                        return;
                    }

                    // Потом — офлайн
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
                    if (!offline.hasPlayedBefore()) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND,
                                "player", name);
                        return;
                    }

                    long diff = System.currentTimeMillis() - offline.getLastSeen();
                    String when = formatDuration(sender, diff);

                    plugin.msg().send(sender, LangKey.SEEN_RECENT,
                            "player", offline.getName(),
                            "value", when);
                })
                .register();
    }

    /** Форматирует миллисекунды в «Xд Xч Xм Xс». */
    private @NotNull String formatDuration(@NotNull Player viewer, long millis) {
        if (millis < 0) {
            String s = plugin.lang().raw(viewer.locale(), LangKey.SEEN_JUST_NOW);
            return s != null ? s : "just now";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;

        LangKey key;
        long main, rest;

        if (days > 0)         { key = LangKey.TIME_FORMAT_DAYS;    main = days;    rest = hours % 24; }
        else if (hours > 0)   { key = LangKey.TIME_FORMAT_HOURS;   main = hours;   rest = minutes % 60; }
        else if (minutes > 0) { key = LangKey.TIME_FORMAT_MINUTES; main = minutes; rest = seconds % 60; }
        else                  { key = LangKey.TIME_FORMAT_SECONDS; main = seconds; rest = 0; }

        String template = plugin.lang().raw(viewer.locale(), key);
        if (template == null) return main + "s";

        // Заменяем вручную — lang.raw возвращает строку, а не Component
        return template
                .replace("<value>", String.valueOf(main))
                .replace("<rest>",  String.valueOf(rest));
    }
}