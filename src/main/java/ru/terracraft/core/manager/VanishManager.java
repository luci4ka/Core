package ru.terracraft.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит UUID vanished-игроков и управляет их видимостью.
 *
 * Ключевая логика:
 *   - enable(player)  → player.hidePlayer(plugin, ...) для всех без права .see
 *                       + setMetadata("vanished", true)
 *   - disable(player) → player.showPlayer(plugin, ...) для всех
 *                       + removeMetadata("vanished")
 *
 * Metadata "vanished" читают сторонние плагины:
 *   - TAB            — скрывает из табло
 *   - ChatChat и др. — через player.canSee() видят невидимость
 *
 * Право "видеть скрытых": terracraft.core.vanish.see
 */
public class VanishManager {

    public static final String PERM_SEE = "terracraft.core.vanish.see";

    /** Metadata-флаг, который читает TAB и другие плагины. */
    public static final String VANISH_METADATA = "vanished";

    private final TerraCorePlugin plugin;

    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

    public VanishManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    // ============ API ============

    public boolean isVanished(@NotNull Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean isVanished(@NotNull UUID uuid) {
        return vanished.contains(uuid);
    }

    /** @return true, если vanish включился. */
    public boolean enable(@NotNull Player player) {
        if (!vanished.add(player.getUniqueId())) return false;

        // 1. Metadata-флаг для TAB и других плагинов
        player.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));

        // 2. Скрыть модель от всех без права .see
        applyVisibility(player);
        return true;
    }

    /** @return true, если vanish выключился. */
    public boolean disable(@NotNull Player player) {
        if (!vanished.remove(player.getUniqueId())) return false;

        // 1. Убрать metadata-флаг
        player.removeMetadata(VANISH_METADATA, plugin);

        // 2. Показать всем, включая тех, кто имел .see
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        return true;
    }

    /** @return true, если vanish включился. */
    public boolean toggle(@NotNull Player player) {
        return isVanished(player) ? !disable(player) : enable(player);
    }

    // ============ Массовые операции ============

    /**
     * Скрыть vanished-игрока от конкретного зрителя.
     * Если зритель имеет право .see — ничего не делаем.
     */
    public void hideFrom(@NotNull Player target, @NotNull Player viewer) {
        if (!isVanished(target)) return;
        if (viewer.hasPermission(PERM_SEE)) return;
        if (viewer.equals(target)) return;
        viewer.hidePlayer(plugin, target);
    }

    /** Показать vanished-игрока конкретному зрителю. */
    public void showTo(@NotNull Player target, @NotNull Player viewer) {
        viewer.showPlayer(plugin, target);
    }

    /** Применить видимость vanished-игрока ко всем онлайн-игрокам. */
    private void applyVisibility(@NotNull Player target) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(target)) continue;
            if (online.hasPermission(PERM_SEE)) {
                online.showPlayer(plugin, target);
            } else {
                online.hidePlayer(plugin, target);
            }
        }
    }

    /** Применить видимость ВСЕХ vanished-игроков конкретному зрителю (после входа). */
    public void applyVisibilityForViewer(@NotNull Player viewer) {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target == null) continue;
            hideFrom(target, viewer);
        }
    }

    /** Применить видимость всех vanished ко всем (после /reload конфига или прав). */
    public void reapplyAll() {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target != null) applyVisibility(target);
        }
    }

    public void clear(@NotNull UUID uuid) {
        vanished.remove(uuid);
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(vanished);
    }

    public int size() {
        return vanished.size();
    }
}