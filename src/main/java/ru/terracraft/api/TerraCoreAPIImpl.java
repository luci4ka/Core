package ru.terracraft.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.CommandKey;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class TerraCoreAPIImpl implements TerraCoreAPI {

    private final TerraCorePlugin plugin;

    public TerraCoreAPIImpl(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    // ============ God ============

    @Override
    public boolean isGod(@NotNull UUID uuid) {
        return plugin.gods().isGod(uuid);
    }

    @Override
    public @NotNull Set<UUID> gods() {
        return plugin.gods().all();
    }

    @Override
    public boolean toggleGod(@NotNull Player player) {
        return plugin.gods().toggle(player);
    }

    // ============ Fly ============

    @Override
    public boolean isFlying(@NotNull UUID uuid) {
        return plugin.fly().isFlying(uuid);
    }

    @Override
    public @NotNull Set<UUID> flying() {
        return plugin.fly().all();
    }

    @Override
    public boolean toggleFly(@NotNull Player player) {
        return plugin.fly().toggle(player);
    }

    // ============ Vanish ============

    @Override
    public boolean isVanished(@NotNull UUID uuid) {
        return plugin.vanish().isVanished(uuid);
    }

    @Override
    public @NotNull Set<UUID> vanished() {
        return plugin.vanish().all();
    }

    @Override
    public boolean toggleVanish(@NotNull Player player) {
        return plugin.vanish().toggle(player);
    }

    // ============ AFK ============

    @Override
    public boolean isAfk(@NotNull UUID uuid) {
        return plugin.afk().isAfk(uuid);
    }

    @Override
    public @NotNull Set<UUID> afk() {
        return plugin.afk().all();
    }

    @Override
    public void setAfk(@NotNull Player player, boolean afk) {
        plugin.afk().set(player, afk);
    }

    @Override
    public long lastActivity(@NotNull UUID uuid) {
        return plugin.afk().lastActivity(uuid);
    }

    // ============ Mail ============

    @Override
    public int unreadMail(@NotNull UUID uuid) {
        return plugin.mail().unreadCount(uuid);
    }

    // ============ Cooldowns ============

    @Override
    public int cooldownRemaining(@NotNull UUID uuid, @NotNull String commandKey) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return 0;
        try {
            CommandKey key = CommandKey.valueOf(commandKey.toUpperCase(Locale.ROOT));
            return plugin.cooldowns().remaining(p, key);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    // ============ Gamemode ============

    @Override
    public @Nullable String gamemode(@NotNull UUID uuid) {
        return plugin.gamemodes().getSavedGamemode(uuid);
    }

    // ============ Bukkit-данные ============

    @Override
    public @Nullable String world(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getWorld().getName() : null;
    }

    @Override
    public int ping(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getPing() : 0;
    }

    @Override
    public double health(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getHealth() : 0;
    }

    @Override
    public int food(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getFoodLevel() : 0;
    }

    @Override
    public boolean isBurning(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null && p.getFireTicks() > 0;
    }

    @Override
    public boolean isFrozen(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null && p.getFreezeTicks() > 0;
    }

    @Override
    public boolean isOp(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null && p.isOp();
    }

    // ============ Новые методы ============

    @Override
    public int level(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getLevel() : 0;
    }

    @Override
    public float exp(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getExp() : 0;
    }

    @Override
    public float saturation(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getSaturation() : 0;
    }

    @Override
    public float walkSpeed(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getWalkSpeed() : 0;
    }

    @Override
    public float flySpeed(@NotNull UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getFlySpeed() : 0;
    }

    @Override
    public boolean canSee(@NotNull Player viewer, @NotNull UUID target) {
        Player t = Bukkit.getPlayer(target);
        return t != null && viewer.canSee(t);
    }
}