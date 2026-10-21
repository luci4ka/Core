package ru.terracraft.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Публичный API TerraCore для использования в других плагинах.
 *
 * <p>Получить экземпляр:
 * <pre>{@code
 * TerraCoreAPI api = Bukkit.getServicesManager().load(TerraCoreAPI.class);
 * }</pre>
 *
 * <p>Чтение — потокобезопасно. Изменение состояний — только из главного потока.
 *
 * <p><b>Онлайн/офлайн:</b> методы, работающие с Bukkit-данными игрока
 * ({@code world}, {@code ping}, {@code health}, {@code food}, {@code level}, {@code exp},
 * {@code saturation}, {@code walkSpeed}, {@code flySpeed}, {@code isBurning}, {@code isFrozen}),
 * возвращают {@code null}/{@code 0}/{@code false} для офлайн-игроков.
 *
 * <p>Методы состояний ({@code god}, {@code fly}, {@code vanish}, {@code afk}, {@code gamemode})
 * работают и для офлайн (читают in-memory set или БД).
 *
 * <p><b>Чувствительные данные</b> (IP, координаты, first/last join) —
 * в {@link TerraCoreAdminAPI}, доступ через право {@code terracraft.core.api.sensitive}.
 */
public interface TerraCoreAPI {

    // ============ Общее ============

    default boolean isOnline(@NotNull UUID uuid) {
        Player p = org.bukkit.Bukkit.getPlayer(uuid);
        return p != null && p.isOnline();
    }

    // ============ God ============

    boolean isGod(@NotNull UUID uuid);

    default boolean isGod(@NotNull Player player) {
        return isGod(player.getUniqueId());
    }

    @NotNull Set<UUID> gods();

    boolean toggleGod(@NotNull Player player);

    // ============ Fly ============

    boolean isFlying(@NotNull UUID uuid);

    default boolean isFlying(@NotNull Player player) {
        return isFlying(player.getUniqueId());
    }

    @NotNull Set<UUID> flying();

    boolean toggleFly(@NotNull Player player);

    // ============ Vanish ============

    boolean isVanished(@NotNull UUID uuid);

    default boolean isVanished(@NotNull Player player) {
        return isVanished(player.getUniqueId());
    }

    @NotNull Set<UUID> vanished();

    boolean toggleVanish(@NotNull Player player);

    // ============ AFK ============

    boolean isAfk(@NotNull UUID uuid);

    default boolean isAfk(@NotNull Player player) {
        return isAfk(player.getUniqueId());
    }

    @NotNull Set<UUID> afk();

    void setAfk(@NotNull Player player, boolean afk);

    long lastActivity(@NotNull UUID uuid);

    // ============ Mail ============

    int unreadMail(@NotNull UUID uuid);

    default int unreadMail(@NotNull Player player) {
        return unreadMail(player.getUniqueId());
    }

    // ============ Cooldowns ============

    int cooldownRemaining(@NotNull UUID uuid, @NotNull String commandKey);

    // ============ Gamemode ============

    @Nullable String gamemode(@NotNull UUID uuid);

    default @Nullable String gamemode(@NotNull Player player) {
        return gamemode(player.getUniqueId());
    }

    // ============ Bukkit-данные (только онлайн) ============

    @Nullable String world(@NotNull UUID uuid);

    default @Nullable String world(@NotNull Player player) {
        return world(player.getUniqueId());
    }

    int ping(@NotNull UUID uuid);

    default int ping(@NotNull Player player) {
        return ping(player.getUniqueId());
    }

    double health(@NotNull UUID uuid);

    default double health(@NotNull Player player) {
        return health(player.getUniqueId());
    }

    int food(@NotNull UUID uuid);

    default int food(@NotNull Player player) {
        return food(player.getUniqueId());
    }

    boolean isBurning(@NotNull UUID uuid);

    default boolean isBurning(@NotNull Player player) {
        return isBurning(player.getUniqueId());
    }

    boolean isFrozen(@NotNull UUID uuid);

    default boolean isFrozen(@NotNull Player player) {
        return isFrozen(player.getUniqueId());
    }

    boolean isOp(@NotNull UUID uuid);

    default boolean isOp(@NotNull Player player) {
        return isOp(player.getUniqueId());
    }

    // ============ Bukkit-данные (новые) ============

    int level(@NotNull UUID uuid);

    float exp(@NotNull UUID uuid);

    float saturation(@NotNull UUID uuid);

    float walkSpeed(@NotNull UUID uuid);

    float flySpeed(@NotNull UUID uuid);

    /**
     * Видит ли {@code viewer} игрока {@code target} в данный момент.
     *
     * <p>Учитывает vanish + {@code hidePlayer} (например, от других плагинов).
     * Для офлайн-{@code target} — {@code false}.
     */
    boolean canSee(@NotNull Player viewer, @NotNull UUID target);
}