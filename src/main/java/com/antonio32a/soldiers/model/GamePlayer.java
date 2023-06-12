package com.antonio32a.soldiers.model;

import com.antonio32a.core.api.player.PlayerCache;
import com.antonio32a.privateapi.data.PlayerProfile;
import com.antonio32a.privateapi.data.SoldiersProfile;
import com.antonio32a.soldiers.event.*;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.handlers.TeamRegistry;
import com.antonio32a.soldiers.util.MapData;
import com.antonio32a.soldiers.util.XPCalculator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Slf4j
public final class GamePlayer {
    public static final int DAMAGE_HISTORY_TTL = 5000;
    private final Player bukkitPlayer;
    private final HashMap<UUID, Long> lastHitBy = new HashMap<>();
    @Setter
    private int maxHealth = 100;
    private int health = 100;
    private boolean hasSpawned = true;

    public GamePlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        respawn();
    }

    @NotNull
    public SoldiersProfile getProfile() {
        UUID uuid = bukkitPlayer.getUniqueId();
        try {
            // We automatically cache the player profile as soon as they join, so this should always be done.
            // The only time this would fail is when some code is referencing a very old GamePlayer instance
            // of a player which left, so the player's profile was removed from the cache after 5 minutes of inactivity.
            CompletableFuture<@Nullable PlayerProfile> future = PlayerCache.INSTANCE.getOrFetch(uuid);
            if (!future.isDone()) throw new IllegalStateException("Player profile of " + uuid + " was not cached");

            @Nullable PlayerProfile playerProfile = future.get();
            if (playerProfile == null) throw new IllegalStateException("Player profile of " + uuid + " is null");

            return playerProfile.getSoldiersProfile();
        } catch (InterruptedException | ExecutionException exception) {
            log.error("Failed to get player profile of player {}", uuid, exception);
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }

    /**
     * @return The player's level.
     */
    public float getLevel() {
        return XPCalculator.getLevel(getXP());
    }

    /**
     * @return The player's total XP.
     */
    public float getXP() {
        return getProfile().getXp();
    }

    /**
     * Sets the player's XP.
     * This will also file appropriate events.
     *
     * @param newXP The new XP.
     */
    public void setXP(float newXP) {
        float oldXP = getProfile().getXp();
        if (oldXP == newXP) return;
        getProfile().setXp(newXP);
        new GamePlayerXPUpdateEvent(this, oldXP, newXP).callEvent();

        float oldLevel = XPCalculator.getLevel(oldXP);
        float newLevel = XPCalculator.getLevel(newXP);
        if (oldLevel != newLevel) {
            new GamePlayerLevelUpdateEvent(this, oldLevel, newLevel).callEvent();
        }
    }

    /**
     * @return The player's remaining XP to the next level.
     */
    public float getRemainingXP() {
        return XPCalculator.getRemainingXP(getXP());
    }

    /**
     * Gets the player's team.
     * Shorthand for {@code TeamRegistry.INSTANCE.getTeam(player)}.
     *
     * @return The player's team.
     */
    @NotNull
    public GameTeam getTeam() {
        return TeamRegistry.INSTANCE.getTeam(bukkitPlayer);
    }

    /**
     * Updates the player's health.
     *
     * @param health The new health value.
     * @param reason The reason for the health update.
     * @return Whether the health was updated.
     */
    public boolean updateHealth(int health, @NotNull GamePlayerHealthUpdateEvent.HealthUpdateReason reason) {
        return updateHealth(health, reason, null);
    }

    /**
     * Updates the player's health.
     *
     * @param health  The new health value.
     * @param reason  The reason for the health update.
     * @param damager The entity which damaged the player. May be null if the player was not damaged or was damaged by themselves.
     * @return Whether the health was updated.
     */
    public boolean updateHealth(
            int health,
            @NotNull GamePlayerHealthUpdateEvent.HealthUpdateReason reason,
            @Nullable Entity damager
    ) {
        return updateHealth(health, reason, damager, true);
    }

    /**
     * Updates the player's health.
     *
     * @param health     The new health value.
     * @param reason     The reason for the health update.
     * @param damager    The entity which damaged the player. May be null if the player was not damaged or was damaged by themselves.
     * @param fakeEffect Whether the player should be shown a fake hurt animation.
     * @return Whether the health was updated.
     */
    public boolean updateHealth(
            int health,
            @NotNull GamePlayerHealthUpdateEvent.HealthUpdateReason reason,
            @Nullable Entity damager,
            boolean fakeEffect
    ) {
        int oldHealth = this.health;
        GamePlayerHealthUpdateEvent event = new GamePlayerHealthUpdateEvent(
                this,
                reason,
                damager,
                oldHealth,
                health
        );

        // We want to update the health before calling the event, so the event can access player.getHealth() directly
        this.health = event.getNewHealth();
        event.callEvent();

        if (event.isCancelled()) {
            this.health = oldHealth;
            return false;
        }

        if (damager instanceof Player && !damager.equals(bukkitPlayer)) {
            lastHitBy.put(damager.getUniqueId(), System.currentTimeMillis());
        }

        if (fakeEffect) {
            // TODO Add support for directional damage
            ServerPlayer handle = ((CraftPlayer) bukkitPlayer).getHandle();
            handle.indicateDamage(0, 0);
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ENTITY_GENERIC_HURT, 1, 1);
        }

        if (this.health <= 0) {
            kill();
        }

        return true;
    }

    /**
     * Respawns the player.
     * This will also teleport them to their spawn point.
     */
    public void respawn() {
        updateHealth(maxHealth, GamePlayerHealthUpdateEvent.HealthUpdateReason.RESPAWN, null, false);
        bukkitPlayer.setVelocity(new Vector(0, 0, 0));
        bukkitPlayer.setFallDistance(0); // Prevents fall damage when respawning
        bukkitPlayer.teleport(MapData.SPAWN.getLocation());
        new GamePlayerSpawnEvent(this, hasSpawned).callEvent();
        hasSpawned = false;
    }

    /**
     * Kills the player.
     * This will also respawn them.
     */
    public void kill() {
        GamePlayerDeathEvent event = new GamePlayerDeathEvent(this);
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }

        @Nullable Map.Entry<UUID, Long> lastHitEntry = lastHitBy.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);

        if (lastHitEntry != null) {
            @Nullable Player killer = Bukkit.getPlayer(lastHitEntry.getKey());
            if (killer != null) {
                GamePlayer killerGamePlayer = PlayerRegistry.INSTANCE.get(killer);
                GamePlayerKillPlayerEvent killEvent = new GamePlayerKillPlayerEvent(killerGamePlayer, this);

                killEvent.callEvent();
                if (killEvent.isCancelled()) {
                    return;
                }
            }
        }

        respawn();
    }
}
