package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.time.Duration;
import java.util.Locale;

/**
 * /gc — TPS, память, чанки, сущности, аптайм.
 *
 * <p>Вывод разбит на блоки: Сервер / Память / Мир.
 * Цвет TPS — по порогам (≥19.5 — зелёный, ≥17 — жёлтый, иначе красный).
 */
public class GcCommand {

    public static final String PERM = "terracraft.core.gc";

    /** Порог «хорошего» TPS. */
    private static final double TPS_GOOD = 19.5;
    /** Порог «среднего» TPS. */
    private static final double TPS_MEDIUM = 17.0;

    private final TerraCorePlugin plugin;

    public GcCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("gc")
                .withAliases("lag", "tps", "mem")
                .withPermission(PERM)
                .executes((sender, args) -> {
                    Locale locale = senderLocale(sender);

                    // === TPS ===
                    double[] tps = Bukkit.getServer().getTPS();
                    String tpsStr = String.format(Locale.ROOT, "%.2f, %.2f, %.2f",
                            tps[0], tps[1], tps[2]);
                    LangKey tpsKey = pickTpsKey(tps[0]);

                    // === Память ===
                    Runtime rt = Runtime.getRuntime();
                    long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                    long maxMb  = rt.maxMemory() / 1024 / 1024;
                    int percent = maxMb > 0 ? (int) Math.round(usedMb * 100.0 / maxMb) : 0;

                    String mb = raw(locale, LangKey.GC_MB);
                    String usedStr = usedMb + " " + mb;
                    String maxStr  = maxMb + " " + mb;

                    // === Чанки ===
                    int chunks = 0;
                    for (World w : Bukkit.getWorlds()) {
                        chunks += w.getLoadedChunks().length;
                    }

                    // === Сущности ===
                    int entities = 0;
                    for (World w : Bukkit.getWorlds()) {
                        entities += w.getEntities().size();
                    }

                    // === Аптайм ===
                    long uptimeMs = System.currentTimeMillis() - plugin.getStartTime();
                    Duration d = Duration.ofMillis(uptimeMs);
                    String uptime = String.format(Locale.ROOT, "%dh %dm %ds",
                            d.toHours(), d.toMinutesPart(), d.toSecondsPart());

                    // === Вывод ===
                    plugin.msg().sendRaw(sender, LangKey.GC_HEADER);
                    plugin.msg().sendRaw(sender, LangKey.GC_SECTION_SERVER);
                    plugin.msg().sendRaw(sender, tpsKey, "value", tpsStr);
                    plugin.msg().sendRaw(sender, LangKey.GC_UPTIME, "value", uptime);

                    plugin.msg().sendRaw(sender, LangKey.GC_SECTION_MEMORY);
                    plugin.msg().sendRaw(sender, LangKey.GC_MEMORY,
                            "used", usedStr,
                            "max", maxStr,
                            "percent", percent);

                    plugin.msg().sendRaw(sender, LangKey.GC_SECTION_WORLD);
                    plugin.msg().sendRaw(sender, LangKey.GC_CHUNKS, "value", chunks);
                    plugin.msg().sendRaw(sender, LangKey.GC_ENTITIES, "value", entities);
                    plugin.msg().sendRaw(sender, LangKey.GC_HEADER);
                })
                .register();
    }

    // ============ Пороги TPS ============

    private @NotNull LangKey pickTpsKey(double tps) {
        if (tps >= TPS_GOOD)   return LangKey.GC_TPS_GOOD;
        if (tps >= TPS_MEDIUM) return LangKey.GC_TPS_MEDIUM;
        return LangKey.GC_TPS_BAD;
    }

    // ============ Утилиты ============

    private @NotNull Locale senderLocale(@NotNull org.bukkit.command.CommandSender sender) {
        return (sender instanceof org.bukkit.entity.Player p) ? p.locale() : Locale.US;
    }

    private @NotNull String raw(@NotNull Locale locale, @NotNull LangKey key) {
        String s = plugin.lang().raw(locale, key);
        return s != null ? s : key.path();
    }
}