package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a game player's XP is updated.
 * XP references our custom XP system, not the vanilla XP system.
 */
@Getter
public final class GamePlayerXPUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer gamePlayer;
    private final float oldXP;
    private final float newXP;

    public GamePlayerXPUpdateEvent(GamePlayer gamePlayer, float oldXP, float newXP) {
        this.gamePlayer = gamePlayer;
        this.oldXP = oldXP;
        this.newXP = newXP;
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
