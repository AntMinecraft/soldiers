package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a game player's level is updated.
 * Level references our custom XP system, not the vanilla XP system.
 */
@Getter
public final class GamePlayerLevelUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer gamePlayer;
    private final float oldLevel;
    private final float newLevel;

    public GamePlayerLevelUpdateEvent(GamePlayer gamePlayer, float oldLevel, float newLevel) {
        this.gamePlayer = gamePlayer;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
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
