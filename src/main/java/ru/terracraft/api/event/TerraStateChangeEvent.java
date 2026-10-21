package ru.terracraft.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Базовое событие смены состояния игрока в TerraCore.
 *
 * <p><b>Не является cancellable:</b> событие вызывается <b>после</b>
 * изменения состояния — отменить действие нельзя.
 *
 * <p>Не вызывайте вручную — события генерируются менеджерами
 * ({@code GodManager}, {@code FlyManager}, {@code VanishManager}, {@code AfkManager})
 * при изменении состояния.
 *
 * <p>Подписка:
 * <pre>{@code
 * @EventHandler
 * public void onGod(TerraGodChangeEvent e) {
 *     if (e.getNewState()) { ... }
 * }
 * }</pre>
 */
public abstract class TerraStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean newState;

    protected TerraStateChangeEvent(@NotNull Player player, boolean newState) {
        this.player = player;
        this.newState = newState;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    /** Новое состояние (true = включено). */
    public boolean getNewState() {
        return newState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}