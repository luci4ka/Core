package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.time.Duration;

/**
 * /gc — TPS, память, чанки, сущности, аптайм.
 */
public class GcCommand {

    public static final String PERM = "terracraft.core.gc";

    private final TerraCorePlugin plugin;

    public GcCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("gc")
                .withAliases("lag", "tps", "mem")
                .withPermission(PERM)
                .executes((sender, args) -> {
                    // TPS
                    double[] tps = Bukkit.getServer().getTPS();
                    String tpsStr = String.format("%.2f, %.2f, %.2f", tps[0], tps[1], tps[2]);

                    // Память
                    Runtime rt = Runtime.getRuntime();
                    long used  = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                    long max   = rt.maxMemory() / 1024 / 1024;

                    // Чанки
                    int chunks = 0;
                    for (World w : Bukkit.getWorlds()) chunks += w.getLoadedChunks().length;

                    // Сущности
                    int entities = 0;
                    for (World w : Bukkit.getWorlds()) {
                        for (Entity e : w.getEntities()) entities++;
                    }

                    // Аптайм
                    long uptimeMs = System.currentTimeMillis() - START_TIME;
                    Duration d = Duration.ofMillis(uptimeMs);
                    String uptime = String.format("%dh %dm %ds", d.toHours(),
                            d.toMinutesPart(), d.toSecondsPart());

                    plugin.msg().sendRaw(sender, LangKey.GC_HEADER);
                    plugin.msg().sendRaw(sender, LangKey.GC_TPS, "value", tpsStr);
                    plugin.msg().sendRaw(sender, LangKey.GC_MEMORY,
                            "used", used + " МБ", "max", max + " МБ");
                    plugin.msg().sendRaw(sender, LangKey.GC_CHUNKS, "value", chunks);
                    plugin.msg().sendRaw(sender, LangKey.GC_ENTITIES, "value", entities);
                    plugin.msg().sendRaw(sender, LangKey.GC_UPTIME, "value", uptime);
                })
                .register();
    }

    private static final long START_TIME = System.currentTimeMillis();
}