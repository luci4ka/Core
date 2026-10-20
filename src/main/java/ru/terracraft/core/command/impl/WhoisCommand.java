package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.util.Args;
import ru.terracraft.core.lang.LangKey;
import ru.terracraft.core.storage.PlayerCacheService;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

public class WhoisCommand {

    public static final String PERM = "terracraft.core.whois";
    public static final String PERM_OTHERS = "terracraft.core.whois.others";

    private final TerraCorePlugin plugin;

    public WhoisCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("whois")
                .withAliases("playerinfo", "pinfo")
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

                    showOffline(sender, name);
                })
                .register();
    }

    private void showOnline(@NotNull CommandSender sender, @NotNull Player p) {
        Locale locale = senderLocale(sender);
        Location l = p.getLocation();

        plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_MAIN);

        plugin.msg().sendRaw(sender, LangKey.WHOIS_NAME, "value", p.getName());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_UUID, "value", p.getUniqueId().toString());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_FIRST_JOIN,
                "value", formatTime(p.getFirstPlayed(), locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_LAST_SEEN,
                "value", raw(locale, LangKey.WHOIS_ONLINE_NOW));

        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_POSITION);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_WORLD, "value", p.getWorld().getName());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_POSITION,
                "value_x", l.getBlockX(),
                "value_y", l.getBlockY(),
                "value_z", l.getBlockZ());

        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_STATUS);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_HEALTH,
                "value", String.format(Locale.ROOT, "%.1f", p.getHealth()));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_FOOD, "value", p.getFoodLevel());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_GAMEMODE,
                "value", localizedGamemode(p.getGameMode(), locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_PING, "value", p.getPing());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_IP, "value", resolveIp(p, locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_OP, "value", boolKey(p.isOp(), locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_FLYING, "value", boolKey(p.isFlying(), locale));

        plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
    }

    private void showOffline(@NotNull CommandSender sender, @NotNull String name) {
        Locale locale = senderLocale(sender);
        PlayerCacheService.PlayerData data = findByCache(name);

        plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_MAIN);

        if (data == null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (!offline.hasPlayedBefore()) {
                plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                return;
            }

            plugin.msg().sendRaw(sender, LangKey.WHOIS_NAME,
                    "value", offline.getName() != null ? offline.getName() : name);
            plugin.msg().sendRaw(sender, LangKey.WHOIS_UUID,
                    "value", offline.getUniqueId().toString());
            plugin.msg().sendRaw(sender, LangKey.WHOIS_FIRST_JOIN,
                    "value", formatTime(offline.getFirstPlayed(), locale));
            plugin.msg().sendRaw(sender, LangKey.WHOIS_LAST_SEEN,
                    "value", formatTime(offline.getLastSeen(), locale));

            plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
            return;
        }

        plugin.msg().sendRaw(sender, LangKey.WHOIS_NAME, "value", data.name());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_UUID, "value", data.uuid().toString());
        plugin.msg().sendRaw(sender, LangKey.WHOIS_FIRST_JOIN,
                "value", formatTime(data.firstJoin(), locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_LAST_SEEN,
                "value", formatTime(data.lastSeen(), locale));

        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_POSITION);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_WORLD,
                "value", data.lastWorld() != null ? data.lastWorld() : unknown(locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_POSITION,
                "value_x", Math.round(data.lastX()),
                "value_y", Math.round(data.lastY()),
                "value_z", Math.round(data.lastZ()));

        plugin.msg().sendRaw(sender, LangKey.WHOIS_SECTION_STATUS);
        plugin.msg().sendRaw(sender, LangKey.WHOIS_GAMEMODE,
                "value", data.lastGamemode() != null
                        ? localizedGamemodeName(data.lastGamemode(), locale)
                        : unknown(locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_IP,
                "value", data.lastIp() != null ? data.lastIp() : unknown(locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_OP, "value", unknown(locale));
        plugin.msg().sendRaw(sender, LangKey.WHOIS_FLYING, "value", unknown(locale));

        plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
    }

    private @Nullable PlayerCacheService.PlayerData findByCache(@NotNull String name) {
        UUID uuid = plugin.playerCache().findUuidByName(name);
        if (uuid == null) return null;
        return plugin.playerCache().get(uuid);
    }

    private @NotNull String formatTime(long millis, @NotNull Locale locale) {
        if (millis <= 0) return unknown(locale);
        DateTimeFormatter fmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());
        return fmt.format(Instant.ofEpochMilli(millis));
    }

    private @NotNull String boolKey(boolean value, @NotNull Locale locale) {
        LangKey key = value ? LangKey.WHOIS_TRUE : LangKey.WHOIS_FALSE;
        String s = plugin.lang().raw(locale, key);
        if (s != null) return s;
        return value ? "<green>✔</green>" : "<red>✘</red>";
    }

    private @NotNull String localizedGamemode(@NotNull GameMode mode, @NotNull Locale locale) {
        LangKey key = switch (mode) {
            case SURVIVAL  -> LangKey.GAMEMODE_NAME_SURVIVAL;
            case CREATIVE  -> LangKey.GAMEMODE_NAME_CREATIVE;
            case ADVENTURE -> LangKey.GAMEMODE_NAME_ADVENTURE;
            case SPECTATOR -> LangKey.GAMEMODE_NAME_SPECTATOR;
        };
        String s = plugin.lang().raw(locale, key);
        return s != null ? s : mode.name().toLowerCase(Locale.ROOT);
    }

    private @NotNull String localizedGamemodeName(@NotNull String rawName, @NotNull Locale locale) {
        try {
            GameMode mode = GameMode.valueOf(rawName.toUpperCase(Locale.ROOT));
            return localizedGamemode(mode, locale);
        } catch (IllegalArgumentException e) {
            return rawName;
        }
    }

    /** IP-адрес игрока. При отсутствии — локализованное «неизвестно». */
    private @NotNull String resolveIp(@NotNull Player p, @NotNull Locale locale) {
        InetSocketAddress addr = p.getAddress();
        if (addr == null || addr.getAddress() == null) return unknown(locale);
        return addr.getAddress().getHostAddress();
    }

    private @NotNull String unknown(@NotNull Locale locale) {
        String s = plugin.lang().raw(locale, LangKey.WHOIS_UNKNOWN);
        return s != null ? s : "<dark_gray>?</dark_gray>";
    }

    private @NotNull String raw(@NotNull Locale locale, @NotNull LangKey key) {
        String s = plugin.lang().raw(locale, key);
        return s != null ? s : "";
    }

    private @NotNull Locale senderLocale(@Nullable CommandSender sender) {
        return (sender instanceof Player p) ? p.locale() : Locale.US;
    }
}