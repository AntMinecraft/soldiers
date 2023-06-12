package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a game player spawns or respawns.
 * This is also initially called when a player joins the server.
 */
@Getter
public final class GamePlayerSpawnEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer gamePlayer;
    private final boolean initialSpawn;

    public GamePlayerSpawnEvent(GamePlayer gamePlayer, boolean initialSpawn) {
        this.gamePlayer = gamePlayer;
        this.initialSpawn = initialSpawn;
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
