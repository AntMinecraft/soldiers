package com.antonio32a.soldiers.item.impl.launcher.projectile;

import com.antonio32a.soldiers.event.GamePlayerHealthUpdateEvent;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.item.impl.launcher.LauncherItem;
import com.antonio32a.soldiers.model.GamePlayer;
import com.antonio32a.soldiers.util.Model;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rocket implements LauncherProjectile {
    private static final int TICKS_TO_LIVE = 20 * 15;
    private final float speedMultiplier;
    private final float radius;
    private final float knockbackMultiplier;
    private final float baseDamage;
    private final float damageFalloff;

    public Rocket(
        float speedMultiplier,
        float radius,
        float knockbackMultiplier,
        float baseDamage,
        float damageFalloff
    ) {
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.knockbackMultiplier = knockbackMultiplier;
        this.baseDamage = baseDamage;
        this.damageFalloff = damageFalloff;
    }

    @NotNull
    @Override
    public String getId() {
        return "rocket";
    }

    @NotNull
    @Override
    public Display createEntity(@NotNull LauncherItem launcher, @NotNull Location location) {
        ItemDisplay entity = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        entity.setItemStack(Model.ROCKET.createStack());
        return entity;
    }

    @Override
    public void onLaunched(@NotNull LauncherItem launcher, @NotNull Entity entity, @NotNull Player shooter) {
        entity.setVelocity(shooter.getEyeLocation().getDirection().multiply(speedMultiplier));
    }

    @Override
    public boolean tick(@NotNull LauncherItem launcher, @NotNull Entity entity) {
        if (
            !entity.getChunk().isLoaded()
                || entity.getTicksLived() > TICKS_TO_LIVE
                || !(entity instanceof Display display)
        ) return false;

        Vector directionVector = display.getLocation().getDirection().multiply(speedMultiplier);
        Location newLocation = display.getLocation().add(directionVector);

        @Nullable Location collideLocation = collidesAt(entity.getLocation(), newLocation, entity);
        if (collideLocation != null) {
            explode(entity, collideLocation, getShooter(entity));
            return false;
        } else {
            // TODO fix interpolation
            // Transformation transformation = display.getTransformation();
            // transformation.getTranslation().set(
            //     display.getLocation().getX() - newLocation.getX(),
            //     display.getLocation().getY() - newLocation.getY(),
            //     display.getLocation().getZ() - newLocation.getZ()
            // );
            // display.setInterpolationDelay(0);
            // display.setTransformation(transformation);
            // display.setInterpolationDuration(1);

            // We must wait a tick for the interpolation to take effect before teleporting it
            // Bukkit.getScheduler().runTaskLater(
            //     Soldiers.getInstance(),
            //     () -> entity.teleport(newLocation),
            //     1L
            // );
            entity.teleport(newLocation);
        }

        return true;
    }

    /**
     * Handle the explosion of the rocket and the damage.
     *
     * @param entity   The entity of the projectile that exploded.
     * @param location The location of the explosion.
     * @param shooter  The player that shot the projectile.
     */
    protected void explode(@NotNull Entity entity, @NotNull Location location, @Nullable Player shooter) {
        spawnExplosionParticles(location);
        for (Player player : location.getWorld().getNearbyPlayers(location, radius)) {
            float falloff = 1.0f - damageFalloff * (float) location.distance(player.getLocation());
            int damage = (int) Math.floor(baseDamage * falloff);
            if (damage <= 0) continue;

            Vector deltaPosition = player.getLocation()
                .toVector()
                .subtract(location.toVector());
            Vector velocity = deltaPosition
                .add(new Vector(0, 0.5, 0))
                .normalize()
                .multiply(new Vector(1.25, 1.1, 1.25))
                .multiply(knockbackMultiplier);
            player.setVelocity(player.getVelocity().add(velocity));

            GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
            gamePlayer.updateHealth(
                gamePlayer.getHealth() - damage,
                GamePlayerHealthUpdateEvent.HealthUpdateReason.DAMAGE_PLAYER,
                shooter
            );
        }
    }

    /**
     * Spawn explosion particles at the given location.
     *
     * @param location The location to spawn the particles at.
     */
    protected void spawnExplosionParticles(@NotNull Location location) {
        location.getWorld().spawnParticle(
            Particle.FLASH,
            location.clone().add(0, 0.5, 0),
            1
        );
    }

    /**
     * Gets where the entity will collide if it moves from the given location to the given location.
     *
     * @param from   The location to move from.
     * @param to     The location to move to.
     * @param entity The entity to check for collisions.
     * @return The location of the collision or null if there is no collision.
     */
    @Nullable
    private Location collidesAt(@NotNull Location from, @NotNull Location to, @NotNull Entity entity) {
        Vector directionVector = to.toVector().subtract(from.toVector());
        double distance = directionVector.length();
        directionVector.normalize();

        for (double i = 0; i < distance; i += 0.1) {
            Location location = from.clone().add(directionVector.clone().multiply(i));
            if (entity.collidesAt(location)) return location;
        }

        return null;
    }
}
