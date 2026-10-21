package ru.terracraft.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.api.event.TerraVanishChangeEvent;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.storage.StateStore;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {

    public static final String PERM_SEE = "terracraft.core.vanish.see";
    public static final String VANISH_METADATA = "vanished";

    private static final String KEY = "vanish";

    private final TerraCorePlugin plugin;
    private final StateStore store;

    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

    public VanishManager(@NotNull TerraCorePlugin plugin, @NotNull StateStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    // ============ API ============

    public boolean isVanished(@NotNull Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean isVanished(@NotNull UUID uuid) {
        return vanished.contains(uuid);
    }

    public boolean enable(@NotNull Player player) {
        if (!vanished.add(player.getUniqueId())) return false;

        player.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
        applyVisibility(player);
        store.set(player.getUniqueId(), KEY, true);
        Bukkit.getPluginManager().callEvent(new TerraVanishChangeEvent(player, true));
        return true;
    }

    public boolean disable(@NotNull Player player) {
        if (!vanished.remove(player.getUniqueId())) return false;
        player.removeMetadata(VANISH_METADATA, plugin);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.canSee(player)) {
                online.showPlayer(plugin, player);
            }
        }
        store.clear(player.getUniqueId(), KEY);
        Bukkit.getPluginManager().callEvent(new TerraVanishChangeEvent(player, false));
        return true;
    }

    public boolean toggle(@NotNull Player player) {
        return isVanished(player) ? !disable(player) : enable(player);
    }

    // ============ Массовые операции ============

    public void hideFrom(@NotNull Player target, @NotNull Player viewer) {
        if (!isVanished(target)) return;
        if (viewer.hasPermission(PERM_SEE)) return;
        if (viewer.equals(target)) return;
        if (!viewer.canSee(target)) return;
        viewer.hidePlayer(plugin, target);
    }

    public void showTo(@NotNull Player target, @NotNull Player viewer) {
        if (viewer.canSee(target)) return;
        viewer.showPlayer(plugin, target);
    }

    private void applyVisibility(@NotNull Player target) {
        boolean hideTab = plugin.config().get().vanish().hideFromTab();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(target)) continue;

            boolean viewerCanSee = online.hasPermission(PERM_SEE);
            if (viewerCanSee) {
                showTo(target, online);
            } else if (hideTab) {
                hideFrom(target, online);
            }
        }
    }

    public void applyVisibilityForViewer(@NotNull Player viewer) {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target != null) {
                hideFrom(target, viewer);
            }
        }
    }

    public void reapplyAll() {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target != null) applyVisibility(target);
        }
    }

    // ============ Восстановление ============

    public void restore(@NotNull Player player) {
        if (isVanished(player)) return;

        if (!store.get(player.getUniqueId(), KEY)) return;

        vanished.add(player.getUniqueId());
        player.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
        applyVisibility(player);
    }

    // ============ Очистка ============

    public void clear(@NotNull UUID uuid) {
        vanished.remove(uuid);
        store.clear(uuid, KEY);
    }

    public void clearMemoryOnly(@NotNull UUID uuid) {
        vanished.remove(uuid);
    }

    public @NotNull Set<UUID> all() {
        return Collections.unmodifiableSet(vanished);
    }

    public int size() {
        return vanished.size();
    }
}