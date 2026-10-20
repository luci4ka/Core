package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;
import ru.terracraft.core.storage.PlayerCacheService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

public class SeenCommand {

    public static final String PERM = "terracraft.core.seen";
    public static final String PERM_OTHERS = "terracraft.core.seen.others";

    private static final long OLD_THRESHOLD_MS = 365L * 24 * 60 * 60 * 1000;

    private final TerraCorePlugin plugin;

    public SeenCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("seen")
                .withPermission(PERM)
                .withArguments(Args.playerArg("name"))
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    if (name == null || name.isBlank()) {
                        plugin.msg().send(sender, LangKey.INVALID_ARGUMENT, "value", "name");
                        return;
                    }

                    Player online = Bukkit.getPlayerExact(name);
                    if (online != null) {
                        showOnline(sender, online);
                        return;
                    }

                    if (!sender.hasPermission(PERM_OTHERS)) {
                        plugin.msg().send(sender, LangKey.NO_PERMISSION);
                        return;
                    }

                    PlayerCacheService.PlayerData data = findByCache(name);
                    long lastSeen;
                    String displayName;

                    if (data != null) {
                        lastSeen = data.lastSeen();
                        displayName = data.name();
                    } else {
                        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
                        if (!offline.hasPlayedBefore()) {
                            plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND,
                                    "player", name);
                            return;
                        }
                        lastSeen = offline.getLastSeen();
                        displayName = offline.getName() != null ? offline.getName() : name;
                    }

                    showOffline(sender, displayName, lastSeen);
                })
                .register();
    }

    private void showOnline(@NotNull CommandSender sender, @NotNull Player p) {
        plugin.msg().sendRaw(sender, LangKey.SEEN_HEADER);
        plugin.msg().sendRaw(sender, LangKey.SEEN_ONLINE, "player", p.getName());
        plugin.msg().sendRaw(sender, LangKey.SEEN_HEADER);
    }

    private void showOffline(@NotNull CommandSender sender,
                             @NotNull String displayName,
                             long lastSeen) {
        Locale locale = senderLocale(sender);
        long diff = System.currentTimeMillis() - lastSeen;

        boolean tooOld = diff > OLD_THRESHOLD_MS || diff < 0;

        plugin.msg().sendRaw(sender, LangKey.SEEN_HEADER);

        if (tooOld) {
            plugin.msg().sendRaw(sender, LangKey.SEEN_LONG_AGO, "player", displayName);
        } else {
            String when = formatDuration(locale, diff);
            plugin.msg().sendRaw(sender, LangKey.SEEN_RECENT,
                    "player", displayName, "value", when);
            plugin.msg().sendRaw(sender, LangKey.SEEN_LAST_JOIN,
                    "value", formatExact(lastSeen, locale));
        }

        plugin.msg().sendRaw(sender, LangKey.SEEN_HEADER);
    }

    private @Nullable PlayerCacheService.PlayerData findByCache(@NotNull String name) {
        UUID uuid = plugin.playerCache().findUuidByName(name);
        if (uuid == null) return null;
        return plugin.playerCache().get(uuid);
    }

    private @NotNull Locale senderLocale(@NotNull CommandSender sender) {
        return (sender instanceof Player p) ? p.locale() : Locale.US;
    }

    private @NotNull String formatDuration(@NotNull Locale locale, long millis) {
        if (millis < 0) {
            String s = plugin.lang().raw(locale, LangKey.SEEN_JUST_NOW);
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

        String template = plugin.lang().raw(locale, key);
        if (template == null) return main + "s";

        return template
                .replace("<value>", String.valueOf(main))
                .replace("<rest>",  String.valueOf(rest));
    }

    private @NotNull String formatExact(long millis, @NotNull Locale locale) {
        if (millis <= 0) {
            String s = plugin.lang().raw(locale, LangKey.SEEN_UNKNOWN);
            return s != null ? s : "?";
        }
        DateTimeFormatter fmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());
        return fmt.format(Instant.ofEpochMilli(millis));
    }
}