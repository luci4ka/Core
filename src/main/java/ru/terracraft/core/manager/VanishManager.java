package ru.terracraft.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.storage.StateStore;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит UUID vanished-игроков и управляет их видимостью.
 *
 * <p>Ключевая логика:
 * <ul>
 *   <li>{@link #enable(Player)} → {@code hidePlayer} для всех без {@link #PERM_SEE}
 *       + metadata {@link #VANISH_METADATA}.</li>
 *   <li>{@link #disable(Player)} → {@code showPlayer} для всех + снятие metadata.</li>
 * </ul>
 *
 * <p>Metadata {@code "vanished"} читают сторонние плагины (TAB, ChatChat и др.).
 *
 * <p>Если {@code storage.type != memory}, флаг сохраняется в БД
 * (таблица {@code states}, state_key = {@value #KEY}). Это даёт режим
 * «vanish навсегда»: игрок остаётся скрытым после рестарта, пока админ не снимет.
 * {@link #restore(Player)} восстанавливает флаг при входе.
 *
 * <p><b>Thread-safety:</b> все публичные методы, меняющие видимость
 * ({@link #enable}, {@link #disable}, {@link #reapplyAll}, {@link #applyVisibilityForViewer}),
 * должны вызываться из главного потока — Bukkit API не потокобезопасен.
 * Хранилище ({@link #vanished}) — {@link ConcurrentHashMap}, читать можно откуда угодно.
 */
public class VanishManager {

    public static final String PERM_SEE = "terracraft.core.vanish.see";

    /** Metadata-флаг, который читает TAB и другие плагины. */
    public static final String VANISH_METADATA = "vanished";

    /** Ключ в таблице states. */
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

    /** @return true, если vanish включился (игрока не было в списке). */
    public boolean enable(@NotNull Player player) {
        if (!vanished.add(player.getUniqueId())) return false;

        player.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
        applyVisibility(player);
        store.set(player.getUniqueId(), KEY, true);
        return true;
    }

    /** @return true, если vanish выключился (игрок был в списке). */

    public boolean disable(@NotNull Player player) {
        if (!vanished.remove(player.getUniqueId())) return false;
        player.removeMetadata(VANISH_METADATA, plugin);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.canSee(player)) {
                online.showPlayer(plugin, player);
            }
        }
        store.clear(player.getUniqueId(), KEY);   // ← было store.set(..., false)
        return true;
    }

    /** @return true, если vanish включился (был выключен). */
    public boolean toggle(@NotNull Player player) {
        return isVanished(player) ? !disable(player) : enable(player);
    }

    // ============ Массовые операции ============

    /**
     * Скрыть vanished-игрока от конкретного зрителя.
     * Если зритель имеет {@link #PERM_SEE} — ничего не делаем.
     */
    public void hideFrom(@NotNull Player target, @NotNull Player viewer) {
        if (!isVanished(target)) return;
        if (viewer.hasPermission(PERM_SEE)) return;
        if (viewer.equals(target)) return;
        if (!viewer.canSee(target)) return;   // уже скрыт
        viewer.hidePlayer(plugin, target);
    }

    /** Показать vanished-игрока конкретному зрителю. */
    public void showTo(@NotNull Player target, @NotNull Player viewer) {
        if (viewer.canSee(target)) return;    // уже видим
        viewer.showPlayer(plugin, target);
    }

    /** Применить видимость vanished-игрока ко всем онлайн-игрокам. */
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
            // Если hideTab == false — не трогаем hidePlayer, TAB скроет по metadata
        }
    }

    /** Применить видимость ВСЕХ vanished-игроков конкретному зрителю (после входа). */
    public void applyVisibilityForViewer(@NotNull Player viewer) {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target != null) {
                hideFrom(target, viewer);
            }
        }
    }

    /**
     * Переприменить видимость всех vanished ко всем.
     * Вызывается из {@code VanishTask} раз в 5 секунд — защита от смены прав в LuckPerms.
     */
    public void reapplyAll() {
        for (UUID id : vanished) {
            Player target = Bukkit.getPlayer(id);
            if (target != null) applyVisibility(target);
        }
    }

    // ============ Восстановление ============

    /**
     * Восстанавливает vanish из БД при входе игрока.
     *
     * <p>Если флаг включён — применяет metadata и видимость (как {@link #enable},
     * но без повторной записи в БД).
     */
    public void restore(@NotNull Player player) {
        if (isVanished(player)) return;

        if (!store.get(player.getUniqueId(), KEY)) return;

        vanished.add(player.getUniqueId());
        player.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
        applyVisibility(player);
    }

    // ============ Очистка ============

    /**
     * Полная очистка: память + БД.
     *
     * <p>Используется при снятии прав/ручной очистке админом.
     * При обычном quit — {@link #clearMemoryOnly(UUID)}.
     */
    public void clear(@NotNull UUID uuid) {
        vanished.remove(uuid);
        store.clear(uuid, KEY);
    }

    /**
     * Убрать из памяти, НЕ трогая БД.
     *
     * <p>Используется в {@code PlayerQuitListener}, если хотите «vanish навсегда»:
     * игрок выходит из игры, но флаг остаётся в БД, и {@link #restore(Player)}
     * вернёт его при следующем входе.
     *
     * <p>Если «vanish навсегда» не нужно — используйте {@link #clear(UUID)}
     * или {@link #disable(Player)} в quit-listener.
     */
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