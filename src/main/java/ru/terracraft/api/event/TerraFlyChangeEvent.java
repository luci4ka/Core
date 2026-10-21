package ru.terracraft.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerraFlyChangeEvent extends TerraStateChangeEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public TerraFlyChangeEvent(@NotNull Player player, boolean newState) {
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