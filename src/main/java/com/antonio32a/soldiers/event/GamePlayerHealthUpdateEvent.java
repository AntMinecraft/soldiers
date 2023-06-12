package com.antonio32a.soldiers.event;

import com.antonio32a.soldiers.model.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a game player's health is updated.
 * This is also called when the player respawns.
 */
@Getter
public final class GamePlayerHealthUpdateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final GamePlayer gamePlayer;
    private final HealthUpdateReason reason;
    /**
     * The entity that damaged the player.
     * This is null if the player damaged themselves or were not damaged at all.
     */
    @Nullable @Getter private final Entity damager;
    private final int oldHealth;
    @Setter private int newHealth;
    @Setter private boolean cancelled;

    public GamePlayerHealthUpdateEvent(
        GamePlayer gamePlayer,
        HealthUpdateReason reason,
        @Nullable Entity damager,
        int oldHealth,
        int newHealth
    ) {
        this.gamePlayer = gamePlayer;
        this.reason = reason;
        this.damager = damager;
        this.oldHealth = oldHealth;
        this.newHealth = newHealth;
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

    /**
     * Represents the reason why a player's health was updated.
     */
    public enum HealthUpdateReason {
        DAMAGE_PLAYER,
        DAMAGE_OTHER,
        DAMAGE_FALL,
        HEAL,
        RESPAWN,
        COMMAND
    }
}
