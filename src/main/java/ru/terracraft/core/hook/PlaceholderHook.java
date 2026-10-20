package ru.terracraft.core.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.config.CommandKey;
import ru.terracraft.core.lang.LangKey;

import java.time.Duration;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Внутренний PlaceholderAPI-расширение TerraCore.
 *
 * <p>Плейсхолдеры — см. javadoc в классе, раздел «PAPI».
 *
 * <p>Если PlaceholderAPI нет — этот класс не регистрируется.
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private static final String COOLDOWN_PREFIX     = "cooldown_";
    private static final String COOLDOWN_RAW_SUFFIX = "_raw";

    // Пороги TPS (как в GcCommand)
    private static final double TPS_GOOD   = 19.5;
    private static final double TPS_MEDIUM = 17.0;

    // Пороги памяти (в %)
    private static final int MEM_GOOD   = 70;
    private static final int MEM_MEDIUM = 85;

    private final TerraCorePlugin plugin;

    public PlaceholderHook(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "terracore";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String key = params.toLowerCase(Locale.ROOT);

        // ==== Общие (не требуют игрока) ====
        if (key.equals("vanished_count")) return String.valueOf(plugin.vanish().size());
        if (key.equals("god_count"))      return String.valueOf(plugin.gods().size());
        if (key.equals("fly_count"))      return String.valueOf(plugin.fly().size());
        if (key.equals("vanished_list"))  return vanishedList();

        // ==== Конфиг (не требует игрока) ====
        AdminToolsConfig cfg = plugin.config().get();
        if (key.equals("enchant_max"))      return String.valueOf(cfg.enchant().maxLevel());
        if (key.equals("give_max"))         return String.valueOf(cfg.give().maxAmount());
        if (key.equals("spawnmob_max"))     return String.valueOf(cfg.spawnMob().maxCount());
        if (key.equals("near_radius"))      return String.valueOf(cfg.near().defaultRadius());
        if (key.equals("compass_duration")) return String.valueOf(cfg.compass().durationSeconds());

        // ==== /gc — сервер (не требует игрока) ====
        if (key.equals("gc_tps")) {
            double[] tps = Bukkit.getServer().getTPS();
            return String.format(Locale.ROOT, "%.2f, %.2f, %.2f", tps[0], tps[1], tps[2]);
        }
        if (key.equals("gc_tps_1m"))  return String.format(Locale.ROOT, "%.2f", Bukkit.getServer().getTPS()[0]);
        if (key.equals("gc_tps_5m"))  return String.format(Locale.ROOT, "%.2f", Bukkit.getServer().getTPS()[1]);
        if (key.equals("gc_tps_15m")) return String.format(Locale.ROOT, "%.2f", Bukkit.getServer().getTPS()[2]);

        if (key.equals("gc_tps_color"))   return tpsColor(Bukkit.getServer().getTPS()[0]);
        if (key.equals("gc_tps_colored")) return tpsColored(Bukkit.getServer().getTPS()[0]);

        if (key.equals("gc_memory_used"))    return String.valueOf(memoryUsedMb());
        if (key.equals("gc_memory_max"))     return String.valueOf(memoryMaxMb());
        if (key.equals("gc_memory_free"))    return String.valueOf(memoryMaxMb() - memoryUsedMb());
        if (key.equals("gc_memory_percent")) return String.valueOf(memoryPercent());
        if (key.equals("gc_memory_colored")) return memoryColored();

        if (key.equals("gc_chunks"))   return String.valueOf(loadedChunks());
        if (key.equals("gc_entities")) return String.valueOf(loadedEntities());

        if (key.equals("gc_uptime"))         return uptimeFormatted();
        if (key.equals("gc_uptime_seconds")) return String.valueOf(uptimeSeconds());
        if (key.equals("gc_uptime_minutes")) return String.valueOf(uptimeSeconds() / 60);
        if (key.equals("gc_uptime_hours"))   return String.valueOf(uptimeSeconds() / 3600);

        // ==== Требуется игрок ====
        if (player == null || !player.isOnline()) return null;
        Player online = player.getPlayer();
        if (online == null) return null;

        // ==== Состояния ====
        if (key.equals("god"))      return String.valueOf(plugin.gods().isGod(online));
        if (key.equals("fly"))      return String.valueOf(plugin.fly().isFlying(online));
        if (key.equals("vanished")) return String.valueOf(plugin.vanish().isVanished(online));

        // ==== Данные игрока ====
        if (key.equals("ping"))             return String.valueOf(online.getPing());
        if (key.equals("ping_color"))       return pingColor(online.getPing());
        if (key.equals("ping_colored"))     return pingColored(online.getPing());
        if (key.equals("world"))            return online.getWorld().getName();
        if (key.equals("gamemode"))         return localizedGamemode(online);
        if (key.equals("gamemode_id"))      return online.getGameMode().name();
        if (key.equals("health"))           return String.format(Locale.ROOT, "%.1f", online.getHealth());
        if (key.equals("health_hearts"))    return String.format(Locale.ROOT, "%.1f", online.getHealth() / 2.0);
        if (key.equals("health_percent"))   return String.format(Locale.ROOT, "%.0f", healthPercent(online));
        if (key.equals("food"))             return String.valueOf(online.getFoodLevel());
        if (key.equals("saturation"))       return String.format(Locale.ROOT, "%.1f", online.getSaturation());
        if (key.equals("level"))            return String.valueOf(online.getLevel());
        if (key.equals("exp"))              return String.format(Locale.ROOT, "%.2f", online.getExp());
        if (key.equals("walk_speed"))       return String.format(Locale.ROOT, "%.2f", online.getWalkSpeed());
        if (key.equals("fly_speed"))        return String.format(Locale.ROOT, "%.2f", online.getFlySpeed());

        // ==== Горение / заморозка ====
        if (key.equals("fire_ticks"))   return String.valueOf(online.getFireTicks());
        if (key.equals("freeze_ticks")) return String.valueOf(online.getFreezeTicks());
        if (key.equals("burning"))      return String.valueOf(online.getFireTicks() > 0);
        if (key.equals("frozen"))       return String.valueOf(online.getFreezeTicks() > 0);

        // ==== Время и погода мира ====
        if (key.equals("world_time"))         return String.valueOf(online.getWorld().getTime());
        if (key.equals("world_time_name"))    return worldTimeName(online);
        if (key.equals("world_weather"))      return worldWeatherKey(online);
        if (key.equals("world_weather_name")) return worldWeatherName(online);

        // ==== Компас ====
        if (key.equals("compass"))           return compassKey(online);
        if (key.equals("compass_localized")) return compassLocalized(online);
        if (key.equals("compass_sector"))    return String.valueOf(compassSector(online));

        // ==== Кулдауны ====
        if (key.startsWith(COOLDOWN_PREFIX)) {
            String rest = key.substring(COOLDOWN_PREFIX.length());
            boolean raw = rest.endsWith(COOLDOWN_RAW_SUFFIX);
            if (raw) rest = rest.substring(0, rest.length() - COOLDOWN_RAW_SUFFIX.length());

            CommandKey commandKey = parseCommandKey(rest);
            if (commandKey == null) return null;

            int remaining = plugin.cooldowns().remaining(online, commandKey);
            if (raw) return String.valueOf(remaining == 0 ? 0 : remaining - 1);
            return String.valueOf(remaining);
        }

        return null;
    }

    // ============ /gc — сервер ============

    private @NotNull String tpsColor(double tps) {
        if (tps >= TPS_GOOD)   return "green";
        if (tps >= TPS_MEDIUM) return "yellow";
        return "red";
    }

    private @NotNull String tpsColored(double tps) {
        String color = tpsColor(tps);
        double[] arr = Bukkit.getServer().getTPS();
        String value = String.format(Locale.ROOT, "%.2f, %.2f, %.2f", arr[0], arr[1], arr[2]);
        return "<" + color + ">" + value + "</" + color + ">";
    }

    private long memoryUsedMb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
    }

    private long memoryMaxMb() {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024;
    }

    private int memoryPercent() {
        long max = memoryMaxMb();
        if (max <= 0) return 0;
        return (int) Math.round(memoryUsedMb() * 100.0 / max);
    }

    private @NotNull String memoryColored() {
        int p = memoryPercent();
        String color;
        if (p <= MEM_GOOD)        color = "green";
        else if (p <= MEM_MEDIUM) color = "yellow";
        else                      color = "red";
        return "<" + color + ">" + p + "%</" + color + ">";
    }

    private int loadedChunks() {
        int chunks = 0;
        for (World w : Bukkit.getWorlds()) {
            chunks += w.getLoadedChunks().length;
        }
        return chunks;
    }

    private int loadedEntities() {
        int entities = 0;
        for (World w : Bukkit.getWorlds()) {
            for (Entity ignored : w.getEntities()) {
                entities++;
            }
        }
        return entities;
    }

    private long uptimeSeconds() {
        return (System.currentTimeMillis() - plugin.getStartTime()) / 1000;
    }

    private @NotNull String uptimeFormatted() {
        long ms = System.currentTimeMillis() - plugin.getStartTime();
        Duration d = Duration.ofMillis(ms);
        return String.format(Locale.ROOT, "%dh %dm %ds",
                d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }

    // ============ Ping ============

    private @NotNull String pingColor(int ping) {
        AdminToolsConfig.Ping cfg = plugin.config().get().ping();
        if (ping < cfg.goodThreshold()) return "green";
        if (ping < cfg.badThreshold())  return "yellow";
        return "red";
    }

    private @NotNull String pingColored(int ping) {
        String color = pingColor(ping);
        return "<" + color + ">" + ping + "</" + color + ">";
    }

    // ============ Vanished list ============

    private @NotNull String vanishedList() {
        StringJoiner sj = new StringJoiner(", ");
        for (var id : plugin.vanish().all()) {
            var p = plugin.getServer().getPlayer(id);
            if (p != null) sj.add(p.getName());
        }
        return sj.toString();
    }

    // ============ World time / weather ============

    private @NotNull String worldTimeName(@NotNull Player player) {
        long ticks = player.getWorld().getTime() % 24000L;
        LangKey key;
        if      (ticks < 1500)  key = LangKey.TIME_NAME_DAY;
        else if (ticks < 9000)  key = LangKey.TIME_NAME_DAY;
        else if (ticks < 12000) key = LangKey.TIME_NAME_NOON;
        else if (ticks < 13800) key = LangKey.TIME_NAME_DUSK;
        else if (ticks < 22200) key = LangKey.TIME_NAME_NIGHT;
        else                    key = LangKey.TIME_NAME_DAWN;

        String s = plugin.lang().raw(player.locale(), key);
        return s != null ? s : key.path();
    }

    private @NotNull String worldWeatherKey(@NotNull Player player) {
        var w = player.getWorld();
        if (w.isThundering()) return "storm";
        if (w.hasStorm())     return "rain";
        return "sun";
    }

    private @NotNull String worldWeatherName(@NotNull Player player) {
        var w = player.getWorld();
        LangKey key;
        if (w.isThundering())  key = LangKey.WEATHER_NAME_STORM;
        else if (w.hasStorm()) key = LangKey.WEATHER_NAME_RAIN;
        else                   key = LangKey.WEATHER_NAME_SUN;

        String s = plugin.lang().raw(player.locale(), key);
        return s != null ? s : key.path();
    }

    // ============ Compass ============

    private int compassSector(@NotNull Player player) {
        float yaw = player.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;
        if      (yaw < 45 || yaw >= 315) return 2;   // S
        else if (yaw < 135)              return 3;   // W
        else if (yaw < 225)              return 0;   // N
        else                             return 1;   // E
    }

    private @NotNull String compassKey(@NotNull Player player) {
        return switch (compassSector(player)) {
            case 0 -> "N";
            case 1 -> "E";
            case 2 -> "S";
            default -> "W";
        };
    }

    private @NotNull String compassLocalized(@NotNull Player player) {
        LangKey key = switch (compassSector(player)) {
            case 0 -> LangKey.DIRECTION_NORTH;
            case 1 -> LangKey.DIRECTION_EAST;
            case 2 -> LangKey.DIRECTION_SOUTH;
            default -> LangKey.DIRECTION_WEST;
        };
        String s = plugin.lang().raw(player.locale(), key);
        return s != null ? s : key.path();
    }

    // ============ Внутреннее ============

    private @NotNull String localizedGamemode(@NotNull Player player) {
        LangKey key = switch (player.getGameMode()) {
            case SURVIVAL  -> LangKey.GAMEMODE_NAME_SURVIVAL;
            case CREATIVE  -> LangKey.GAMEMODE_NAME_CREATIVE;
            case ADVENTURE -> LangKey.GAMEMODE_NAME_ADVENTURE;
            case SPECTATOR -> LangKey.GAMEMODE_NAME_SPECTATOR;
        };
        String s = plugin.lang().raw(player.locale(), key);
        return s != null ? s : player.getGameMode().name();
    }

    private double healthPercent(@NotNull Player player) {
        AttributeInstance maxAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxAttr != null ? maxAttr.getValue() : 20.0;
        if (max <= 0) return 0;
        return Math.min(100.0, player.getHealth() / max * 100.0);
    }

    private @Nullable CommandKey parseCommandKey(@NotNull String name) {
        try {
            return CommandKey.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}