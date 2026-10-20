package ru.terracraft.core.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

/**
 * ASCII-панель запуска/выключения TerraCore.
 *
 * <p>При старте выводит:
 * <ul>
 *   <li>версию, сервер, Java, OS, время загрузки;</li>
 *   <li>компоненты: Storage, Languages, Commands, PlaceholderAPI, bStats, Updater;</li>
 *   <li>статус: ready / with warnings / check log.</li>
 * </ul>
 */
public final class StartupBanner {

    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String DIM     = "\u001B[2m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";

    private static final int LINE_WIDTH = 60;

    private StartupBanner() {}

    // ============ STARTUP ============

    public static void printStartup(@NotNull TerraCorePlugin plugin,
                                    long elapsedMs,
                                    int bStatsId) {
        String version = plugin.getPluginMeta().getVersion();
        String server  = Bukkit.getName() + " " + Bukkit.getVersion();
        String java    = System.getProperty("java.version");
        String os      = System.getProperty("os.name") + " " + System.getProperty("os.arch");

        log("");
        log(DIM + "  ╭" + "─".repeat(LINE_WIDTH) + "╮" + RESET);
        log(DIM + "  │" + RESET + "  " + BOLD + CYAN + "TerraCore" + RESET
                + DIM + "  ·  Admin utilities for Paper" + RESET);
        log(DIM + "  ╰" + "─".repeat(LINE_WIDTH) + "╯" + RESET);
        log("");

        line("Version",   version);
        line("Server",    server);
        line("Java",      java);
        line("OS",        os);
        line("Load time", Math.max(1, elapsedMs) + " ms");
        log("");

        // ==== Компоненты ====
        boolean commandApi = isPluginPresent("CommandAPI");
        boolean papi       = isPluginPresent("PlaceholderAPI");
        boolean mineSql    = isPluginPresent("MineSQL");

        boolean storageOk = mineSql
                && plugin.storage() != null
                && plugin.storage().sqlOrNull() != null;

        String storageDetail;
        if (!mineSql)         storageDetail = "MineSQL missing — disabled";
        else if (!storageOk)  storageDetail = "MineSQL failed — check logs";
        else                  storageDetail = "MineSQL (" + plugin.config().get().storage().tablePrefix() + ")";
        component("Storage", storageOk, storageDetail);

        boolean langOk = plugin.lang() != null && !plugin.lang().locales().isEmpty();
        String langDetail = langOk
                ? plugin.lang().locales().size() + " loaded"
                : "not loaded";
        component("Languages", langOk, langDetail);

        component("Commands", commandApi,
                commandApi ? "registered" : "CommandAPI missing — skipped");

        component("PlaceholderAPI", papi,
                papi ? "hook active" : "not installed (optional)");

        boolean bStats = plugin.config().get().integrations().bStats();
        component("bStats", bStats,
                bStats
                        ? (bStatsId > 0 ? "id=" + bStatsId : "id not set — disabled")
                        : "disabled in config");

        boolean updater = plugin.config().get().integrations().updateNotifier();
        component("Updater", updater,
                updater ? "checking GitHub" : "disabled in config");

        log("");
        log(DIM + "  " + "─".repeat(LINE_WIDTH) + RESET);

        // ==== Итог ====
        int warnings = 0;
        if (!storageOk) warnings++;
        if (!langOk)    warnings++;
        if (!commandApi) warnings++;

        if (warnings == 0) {
            log("  " + GREEN + BOLD + "✓ TerraCore enabled" + RESET + DIM + " — ready." + RESET);
        } else {
            log("  " + YELLOW + BOLD + "⚠ TerraCore enabled with " + warnings
                    + " warning(s)" + RESET + DIM + " — check log." + RESET);
        }

        log(DIM + "  " + "─".repeat(LINE_WIDTH) + RESET);
        log("");
    }

    // ============ SHUTDOWN ============

    public static void printShutdown(@NotNull TerraCorePlugin plugin, long uptimeMs) {
        printShutdown(plugin, uptimeMs, null);
    }

    /**
     * @param reason причина выключения (например, "server stop", "reload") — опционально.
     */
    public static void printShutdown(@NotNull TerraCorePlugin plugin,
                                     long uptimeMs,
                                     @Nullable String reason) {
        long s = uptimeMs / 1000;
        long m = s / 60;
        long h = m / 60;

        String uptime = String.format("%dh %dm %ds", h, m % 60, s % 60);
        String extra = (reason == null || reason.isBlank())
                ? ""
                : DIM + " (" + reason + ")" + RESET;

        log("");
        log(DIM + "  " + "─".repeat(LINE_WIDTH) + RESET);
        log("  " + YELLOW + "TerraCore disabled" + RESET
                + DIM + " — uptime " + uptime + RESET + extra);
        log(DIM + "  " + "─".repeat(LINE_WIDTH) + RESET);
        log("");
    }

    // ============ HELPERS ============

    private static void line(@NotNull String key, @NotNull String value) {
        log("  " + DIM + pad(key, 14) + RESET + " " + value);
    }

    private static void component(@NotNull String name, boolean ok, @NotNull String detail) {
        String icon = ok ? GREEN + "✓" + RESET : RED + "✗" + RESET;
        log("  " + icon + " " + pad(name, 18) + DIM + detail + RESET);
    }

    private static boolean isPluginPresent(@NotNull String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    private static @NotNull String pad(@NotNull String s, int len) {
        if (s.length() >= len) return s;
        return s + " ".repeat(len - s.length());
    }

    private static void log(@NotNull String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}