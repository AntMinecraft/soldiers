package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GameTeam;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a round ends.
 * This event is called before the round is reset, so calling {@link GameTeam#getScore()} will still work..
 */
@Getter
public final class RoundEndEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    /**
     * The team which won the round.
     * If the round ended in a draw, this will be null.
     */
    @Nullable private final GameTeam winningTeam;
    private final boolean draw;

    public RoundEndEvent(@Nullable GameTeam winningTeam, boolean draw) {
        this.winningTeam = winningTeam;
        this.draw = draw;
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
