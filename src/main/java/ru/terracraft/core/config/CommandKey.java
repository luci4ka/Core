package ru.terracraft.core.config;

import org.jetbrains.annotations.NotNull;

/**
 * Ключи команд, для которых можно задать кулдаун.
 * <p>
 * Использование:
 *   plugin.cooldowns().check(player, CommandKey.GOD);
 * <p>
 * id() — строковое представление для .lang и логов: GOD → "god".
 */
public enum CommandKey {
    GOD,
    FLY,
    HEAL,
    FEED,
    REPAIR,
    HAT,
    WORKBENCH,
    ENDERCHEST,
    VANISH,
    SPAWNMOB;

    public @NotNull String id() {
        return name().toLowerCase();
    }
}