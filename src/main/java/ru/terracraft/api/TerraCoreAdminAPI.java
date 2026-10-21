package ru.terracraft.api;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.api.dto.PlayerSnapshot;

import java.util.UUID;

/**
 * Админский API TerraCore — доступ к чувствительным данным
 * (IP, координаты, first/last join).
 *
 * <p>Каждый метод требует права {@value #PERM} у {@code viewer}.
 * Если права нет — возвращает {@code null} / {@code 0}.
 *
 * <p>Получить:
 * <pre>{@code
 * TerraCoreAdminAPI admin = Bukkit.getServicesManager().load(TerraCoreAdminAPI.class);
 * String ip = admin.ip(sender, targetUuid);
 * }</pre>
 */
public interface TerraCoreAdminAPI {

    String PERM = "terracraft.core.api.sensitive";

    // ============ IP ============

    /** IP-адрес игрока или null (нет прав / офлайн). */
    @Nullable String ip(@NotNull CommandSender viewer, @NotNull UUID target);

    // ============ Координаты ============

    record Position(double x, double y, double z, @NotNull String world) {}

    /** Координаты игрока или null (нет прав / офлайн). */
    @Nullable Position position(@NotNull CommandSender viewer, @NotNull UUID target);

    // ============ Время ============

    /** Первый вход (epoch ms) или 0. */
    long firstJoin(@NotNull CommandSender viewer, @NotNull UUID target);

    /**
     * Последний раз видели:
     * для онлайн — сейчас, для офлайн — время выхода.
     * 0 — если нет прав.
     */
    long lastSeen(@NotNull CommandSender viewer, @NotNull UUID target);

    // ============ Кэш игрока (from DB) ============

    /** Данные офлайн-кэша (из БД). null — если нет прав / записи нет. */
    @Nullable PlayerSnapshot cache(@NotNull CommandSender viewer, @NotNull UUID target);
}