package ru.terracraft.api.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Публичный DTO для данных офлайн-игрока.
 */
public record PlayerSnapshot(
        @NotNull UUID uuid,
        @NotNull String name,
        long firstJoin,
        long lastSeen,
        @Nullable String lastIp,
        @Nullable String lastWorld,
        double lastX,
        double lastY,
        double lastZ,
        @Nullable String lastGamemode
) {}