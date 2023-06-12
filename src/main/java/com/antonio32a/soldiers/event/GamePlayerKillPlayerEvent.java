package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after {@link GamePlayerDeathEvent} (if the victim was killed by a player) when a game player kills another game player.
 * This is also called for indirect kills, such as when the victim takes lethal fall damage after being hit by the killer.
 * This will not fire if the killer is the same as the victim.
 * If cancelling make sure to update their health.
 */
@Getter
public final class GamePlayerKillPlayerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer killer;
    private final GamePlayer victim;
    @Setter private boolean cancelled;

    public GamePlayerKillPlayerEvent(GamePlayer killer, GamePlayer victim) {
        this.killer = killer;
        this.victim = victim;
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
