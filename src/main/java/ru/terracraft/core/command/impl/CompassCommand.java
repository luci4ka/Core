package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /compass — показывает направление в BossBar.
 * Повторный вызов — выключает.
 *
 * <p>Стиль: {@code N · · · ● · · · E · · · S · · · W},
 * где {@code ●} стоит над текущим направлением.
 *
 * <p>Настройки — в {@code config.yml}, раздел {@code compass}.
 */
public class CompassCommand {

    public static final String PERM = "terracraft.core.compass";

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String[] MARKS  = {"N", "E", "S", "W"};
    private static final String[] COLORS = {"<red>", "<gold>", "<green>", "<aqua>"};
    private static final LangKey[] KEYS  = {
            LangKey.DIRECTION_NORTH,
            LangKey.DIRECTION_EAST,
            LangKey.DIRECTION_SOUTH,
            LangKey.DIRECTION_WEST
    };

    private final TerraCorePlugin plugin;
    private final Map<UUID, CompassSession> sessions = new ConcurrentHashMap<>();

    public CompassCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("compass")
                .withPermission(PERM)
                .executesPlayer((player, args) -> {
                    CompassSession existing = sessions.remove(player.getUniqueId());
                    if (existing != null) {
                        existing.stop(player);
                        return;
                    }
                    CompassSession session = new CompassSession(player);
                    sessions.put(player.getUniqueId(), session);
                    session.start();
                })
                .register();
    }

    // ============ Внутреннее ============

    private final class CompassSession {
        private final Player player;
        private BossBar bar;
        private BukkitTask updateTask;
        private BukkitTask endTask;

        CompassSession(Player player) {
            this.player = player;
        }

        void start() {
            AdminToolsConfig.Compass cfg = plugin.config().get().compass();

            BossBar.Color color = parseColor(cfg.color());
            BossBar.Overlay overlay = parseOverlay(cfg.overlay());

            this.bar = BossBar.bossBar(render(), 1.0f, color, overlay);
            player.showBossBar(bar);

            this.updateTask = plugin.getServer().getScheduler().runTaskTimer(
                    plugin,
                    () -> {
                        if (!player.isOnline()) {
                            stop(player);
                            return;
                        }
                        bar.name(render());
                    },
                    0L, Math.max(1, cfg.updatePeriodTicks())
            );

            // durationSeconds = 0 → бесконечно (без endTask)
            if (cfg.durationSeconds() > 0) {
                this.endTask = plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> stop(player),
                        cfg.durationSeconds() * 20L
                );
            }
        }

        void stop(@NotNull Player p) {
            if (updateTask != null) updateTask.cancel();
            if (endTask != null) endTask.cancel();
            if (bar != null) p.hideBossBar(bar);
            sessions.remove(p.getUniqueId());
        }

        private @NotNull Component render() {
            float yaw = player.getLocation().getYaw();
            yaw = (yaw % 360 + 360) % 360;

            int sector;
            if      (yaw < 45 || yaw >= 315) sector = 2;   // S
            else if (yaw < 135)              sector = 3;   // W
            else if (yaw < 225)              sector = 0;   // N
            else                             sector = 1;   // E

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i == sector) {
                    sb.append(COLORS[i])
                            .append("<bold>").append(MARKS[i]).append("</bold>")
                            .append("</").append(COLORS[i].substring(1));
                } else {
                    sb.append("<gray>").append(MARKS[i]).append("</gray>");
                }

                if (i < 3) {
                    sb.append(" <dark_gray>·</dark_gray>");
                    if (i == sector) {
                        sb.append(" <white>●</white>");
                    } else {
                        sb.append(" <dark_gray>·</dark_gray>");
                    }
                    sb.append(" <dark_gray>·</dark_gray> ");
                }
            }

            LangKey key = KEYS[sector];
            String localized = plugin.lang().raw(player.locale(), key);
            if (localized == null) localized = MARKS[sector];

            sb.append(" <dark_gray>—</dark_gray> <yellow>")
                    .append(localized)
                    .append("</yellow>");

            return MM.deserialize(sb.toString());
        }
    }

    // ============ Парсеры ============

    private static @NotNull BossBar.Color parseColor(@NotNull String raw) {
        try {
            return BossBar.Color.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.BLUE;
        }
    }

    private static @NotNull BossBar.Overlay parseOverlay(@NotNull String raw) {
        try {
            return BossBar.Overlay.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }
}