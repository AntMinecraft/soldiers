package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a game player dies.
 * If cancelling make sure to update their health.
 */
@Getter
public final class GamePlayerDeathEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer gamePlayer;
    @Setter private boolean cancelled;

    public GamePlayerDeathEvent(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @SuppressWarnings("unused") // Needed for custom events
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
