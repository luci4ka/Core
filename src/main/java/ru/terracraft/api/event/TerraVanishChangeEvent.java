package ru.terracraft.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerraVanishChangeEvent extends TerraStateChangeEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public TerraVanishChangeEvent(@NotNull Player player, boolean newState) {
        super(player, newState);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}